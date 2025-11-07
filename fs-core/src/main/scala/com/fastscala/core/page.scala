package com.fastscala.core

import com.fastscala.core.FSContext.logger
import com.fastscala.js.{Js, JsUtils}
import com.fastscala.routing.RoutingHandlerNoSessionHelper
import com.fastscala.routing.req.{Get, Post}
import com.fastscala.routing.resp.*
import com.fastscala.stats.{FSStats, StatEvent}
import com.fastscala.utils.{IdGen, Missing}
import com.typesafe.config.ConfigFactory
import it.unimi.dsi.util.XoRoShiRo128PlusRandom
import org.eclipse.jetty.http.*
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.{Request, Response as JettyServerResponse}
import org.eclipse.jetty.util.Callback
import org.eclipse.jetty.websocket.api.Callback.Completable
import org.eclipse.jetty.websocket.api.Session
import org.slf4j.LoggerFactory

import java.net.URLEncoder
import java.nio.file.{Files, Path}
import java.util.Collections
import scala.jdk.CollectionConverters.{IterableHasAsScala, MapHasAsScala}
import scala.util.chaining.scalaUtilChainingOps


object FSPage {
  val logger = LoggerFactory.getLogger(getClass.getName)
}

/** Your own page implementation (should be a subclass)
 * @tparam R
 *   The result type of rendering the page
 */
trait FSPageImpl[R] {
  def render()(implicit fsc: FSContext): R
}

trait NoFSPageImplProvided[T] extends FSPageImpl[T]

class FSPage(
              val id: String,
              val session: FSSession,
              val req: Request,
              val impl: FSPageImpl[?],
              val createdAt: Long = System.currentTimeMillis(),
              val onPageUnload: () => Js = () => JS.void,
              var keepAliveAt: Long = System.currentTimeMillis(),
              var autoKeepAliveAt: Long = System.currentTimeMillis(),
              var wsSession: Option[Session] = None,
              var wsQueue: List[Js] = Nil,
              var wsLock: AnyRef = new AnyRef,
              val debugLbl: Option[String] = None
            ) extends FSHasSession {

  def periodicKeepAliveEnabled: Boolean = session.fsSystem.config.getBoolean("com.fastscala.core.periodic-page-keep-alive.enabled")

  def periodicKeepAlivePeriod: Long = session.fsSystem.config.getLong("com.fastscala.core.periodic-page-keep-alive.period-millis")

  def periodicKeepAliveDefunctAfter: Long = session.fsSystem.config.getLong("com.fastscala.core.periodic-page-keep-alive.defunct-after-millis")

  private implicit def __fsContextOpt: Option[FSContext] = None

  private implicit def __fsPageOpt: Option[FSPage] = Some(this)

  private implicit def __fsSessionOpt: Option[FSSession] = Some(session)

  private implicit def __fsSystem: FSSystem = session.fsSystem

  def keepAlive(): Unit = {
    keepAliveAt = System.currentTimeMillis()
    session.keepAlive()
  }

  def allKeepAlivesIterable: Iterable[Long] =
    callbacks.values.map(_.keepAliveAt) ++
      fileUploadCallbacks.values.map(_.keepAliveAt) ++
      fileDownloadCallbacks.values.map(_.keepAliveAt) ++
      Iterable(keepAliveAt)

  val key2FSContext = collection.mutable.Map[AnyRef, FSContext]()

  val callbacks = collection.mutable.Map[String, FSFunc]()
  val deletedCallbacksContextPath = collection.mutable.Map[String, String]()
  val fileUploadCallbacks = collection.mutable.Map[String, FSFileUpload]()
  val fileDownloadCallbacks = collection.mutable.Map[String, FSFileDownload]()

  val rootFSContext = new FSContext(session, this, onPageUnload = onPageUnload, debugLbl = Some("page_root_context"))

  def inContextFor[T](contextFor: AnyRef)(f: FSContext => T): T = {
    if (!key2FSContext.contains(contextFor)) throw new Exception(s"Trying to get context for $contextFor, but wasn't found")
    if (key2FSContext(contextFor).deleted) throw new Exception(s"Trying to get context for $contextFor, but it was deleted")
    f(key2FSContext(contextFor))
  }

  def inContextForOption[T](contextFor: AnyRef)(f: FSContext => T): Option[T] = {
    if (!key2FSContext.contains(contextFor)) None
    if (key2FSContext(contextFor).deleted) None
    Some(f(key2FSContext(contextFor)))
  }

  def deleteContextFor(key: AnyRef): Unit = {
    key2FSContext.get(key).foreach(existing => {
      if (existing.deleted) {
        logger.trace(s"ALREADY DELETED CONTEXT ${existing.fullPath} ($existing)")
      } else {
        logger.trace(s"DELETING CONTEXT ${existing.fullPath} ($existing)")
        existing.delete()
      }
    })
  }

  def deleteOlderThan(ts: Long): Unit = rootFSContext.deleteOlderThan(ts)

  def delete(): Unit = {
    callbacks.filterInPlace { (_, f) =>
      f.fsc.functionsGenerated -= id
      false
    }
    session.fsSystem.stats.currentCallbacks.dec(callbacks.size)

    fileUploadCallbacks.filterInPlace { (_, f) =>
      f.fsc.functionsFileUploadGenerated -= id
      false
    }
    session.fsSystem.stats.currentFileUploadCallbacks.dec(fileUploadCallbacks.size)

    fileDownloadCallbacks.filterInPlace { (_, f) =>
      f.fsc.functionsFileDownloadGenerated -= id
      false
    }
    session.fsSystem.stats.currentFileDownloadCallbacks.dec(fileDownloadCallbacks.size)
  }

  def callbackClientSideBefore(implicit fsc: FSContext): Js = session.fsSystem.callbackClientSideBefore

  def callbackClientSideAfter(implicit fsc: FSContext): Js = session.fsSystem.callbackClientSideAfter

  def callbackClientSideOnError(implicit fsc: FSContext): Js = session.fsSystem.callbackClientSideOnError

  def callbackClientSideOnTimeout(implicit fsc: FSContext): Js = session.fsSystem.callbackClientSideOnTimeout

  def fsPageScript(openWSSessionAtStart: Boolean = false)(implicit fsc: FSContext): Js = {
    JS {
      s"""window._fs = {
         |  sessionId: ${JS.asJsStr(session.id).cmd},
         |  pageId: ${JS.asJsStr(id).cmd},
         |  callback: function(arg, pageId, funcId, ignoreErrors, async, expectReturn, env) {
         |    const xhr = new XMLHttpRequest();
         |    xhr.open("POST", "/${session.fsSystem.FSPrefix}/cb/"+pageId+"/"+funcId+"?time=" + new Date().getTime() + (ignoreErrors ? "&ignore_errors=true" : ""), async);
         |    xhr.setRequestHeader("Content-type", "text/plain;charset=utf-8");
         |    if (expectReturn) {
         |      xhr.onload = function() { try {$callbackClientSideAfter; eval(this.responseText);} catch(err) { $callbackClientSideOnError; console.log(err.message); console.log('While runnning the code:\\n' + this.responseText); } };
         |    }
         |    xhr.ontimeout = (e) => { $callbackClientSideOnTimeout };
         |    $callbackClientSideBefore
         |    xhr.send(arg);
         |  },
         |  keepAlive: function(pageId) {
         |    const xhr = new XMLHttpRequest();
         |    xhr.open("POST", "/${session.fsSystem.FSPrefix}/ka/"+pageId+"?time=" + new Date().getTime(), true);
         |    xhr.setRequestHeader("Content-type", "text/plain;charset=utf-8");
         |    xhr.onload = function() { try {eval(this.responseText);} catch(err) { console.log(err.message); console.log('While runnning the code:\\n' + this.responseText); } };
         |    xhr.send('');
         |  },
         |  initWebSocket: function() {
         |    if(!window._fs.ws) {
         |      window._fs.ws = new WebSocket(location.origin.replace(/^http/, 'ws') + "/${session.fsSystem.FSPrefix}/ws?sessionId=${URLEncoder.encode(
        fsc.session.id,
        "UTF-8"
      )}&pageId=${URLEncoder.encode(fsc.page.id, "UTF-8")}", "fs");
         |      window._fs.ws.onmessage = function(event) {
         |        try {eval(event.data);} catch(err) { console.log(err.message); console.log('While runnning the code:\\n' + event.data); }
         |      };
         |      window._fs.ws.onclose = function(){
         |        window._fs.ws = null
         |        setTimeout(window._fs.initWebSocket, 1000);
         |      };
         |    }
         |  },
         |};
         |${if (openWSSessionAtStart) initWebSocket().cmd else JS.void.cmd}
         |${if (periodicKeepAliveEnabled) setupKeepAlive().cmd else JS.void.cmd}
         |""".stripMargin
    }
  }

  def isDefunct_? = periodicKeepAliveEnabled && (System.currentTimeMillis() - autoKeepAliveAt) > periodicKeepAliveDefunctAfter

  def isAlive_? = !isDefunct_?

  def setupKeepAlive(): Js = JS(s"""function sendKeepAlive() {window._fs.keepAlive(${JS.asJsStr(id).cmd});setTimeout(sendKeepAlive, ${periodicKeepAlivePeriod});};sendKeepAlive();""")

  def initWebSocket() = JS("window._fs.initWebSocket();")
}
