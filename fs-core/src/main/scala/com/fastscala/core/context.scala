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

object FSContext {
  val logger = LoggerFactory.getLogger(getClass.getName)
}

class FSContext(
  val session: FSSession,
  val page: FSPage,
  val parentFSContext: Option[FSContext] = None,
  val onPageUnload: () => Js = () => JS.void,
  var keepAliveAt: Long = System.currentTimeMillis(),
  val debugLbl: Option[String] = None,
  var deletedAt: Option[Long] = None
) extends FSHasSession {

  def deleteOlderThan(ts: Long): Unit = {
    if (keepAliveAt < ts) delete()
    else children.foreach(_.deleteOlderThan(ts))
  }

  def keepAlive(): Unit = {
    Iterator.iterate(Option(this))(_.flatMap(_.parentFSContext)).takeWhile(_.isDefined).foreach(_.foreach(_.keepAliveAt = System.currentTimeMillis()))
    page.keepAlive()
  }

  def deleted: Boolean = deletedAt.isDefined

  import FSContext.logger

  def depth: Int = parentFSContext.map(_.depth + 1).getOrElse(0)

  implicit def fsc: FSContext = this

  private implicit def __fsContextOpt: Option[FSContext] = Some(this)

  private implicit def __fsPageOpt: Option[FSPage] = Some(page)

  private implicit def __fsSessionOpt: Option[FSSession] = Some(session)

  private implicit def __fsSystem: FSSystem = session.fsSystem

  val functionsGenerated = collection.mutable.Set[String]()
  val functionsFileUploadGenerated = collection.mutable.Set[String]()
  val functionsFileDownloadGenerated = collection.mutable.Set[String]()
  val children = collection.mutable.Set[FSContext]()

  def fullPath: String = Iterator.unfold(Option(this))(_.map(ctx => (ctx, ctx.parentFSContext))).toSeq.reverse.map(ctx =>
    ctx.debugLbl.getOrElse(s"anon[${ctx.##.toHexString}]").pipe(name => if (ctx.deleted) s"[del: $name]" else name)
  ).mkString(" => ")

  def sendToPages(f: PartialFunction[(FSPage, FSPageImpl[_]), FSContext => Js]): Unit = {
    for {
      (_, session) <- page.session.fsSystem.sessions
      page <- session.pages.values
      if f.isDefinedAt((page, page.impl))
    } {
      try {
        page.rootFSContext.sendToPage(f.apply((page, page.impl))(page.rootFSContext))
      } catch {
        case ex: Exception => ex.printStackTrace()
      }
    }
  }

  def sendToPage(js: Js): Unit = {
    page.wsLock.synchronized {
      page.wsSession.filter(_.isOpen) match {
        case Some(session) =>
          try {
            // block until session complete sendText
            Completable.`with`(cb => session.sendText((js :: page.wsQueue).reverse.reduce(_ & _).cmd, cb)).get()
            page.wsQueue = Nil
          } catch {
            case ex: Exception =>
              page.wsQueue = js :: page.wsQueue
              throw ex
          }
        case None => page.wsQueue = js :: page.wsQueue
      }
    }
  }

  def runInNewOrRenewedChildContextFor[T](contextFor: AnyRef, debugLabel: Option[String] = None)(f: FSContext => T): T = {
    // if (deleted) throw new Exception(s"Trying to create child of deleted context ($fullPath)")
    // If it already exists, delete:
    page.deleteContextFor(contextFor)

    val newContext = new FSContext(session, page, Some(this), debugLbl = debugLabel)
    page.key2FSContext(contextFor) = newContext
    logger.trace(s"Creating context ${newContext.fullPath} ($newContext)")
    children += newContext
    f(newContext)
  }

  def runInContextFor[T](contextFor: AnyRef, debugLabel: Option[String] = None)(f: FSContext => T): T = {
    if (deleted) throw new Exception(s"Trying to create child of deleted context ($fullPath)")
    // If it already exists, delete:
    val ctx = page.key2FSContext.getOrElseUpdate(
      contextFor, {
        val newContext = new FSContext(session, page, Some(this), debugLbl = debugLabel.orElse(Some(s"ctx4$contextFor")))
        page.key2FSContext(contextFor) = newContext
        logger.trace(s"Creating context ${newContext.fullPath} ($newContext)")
        children += newContext
        newContext
      }
    )
    f(ctx)
  }

  def delete(): Unit = {
    logger.debug(s"Delete context $fullPath")
    deletedAt = Some(System.currentTimeMillis())
    parentFSContext.foreach(_.children -= this)
    if (__fsSystem.debug) {
      val path = this.fullPath
      page.deletedCallbacksContextPath ++= functionsGenerated.map(id => id -> path)
    }
    page.callbacks --= functionsGenerated
    session.fsSystem.stats.event(StatEvent.GC_CALLBACK, n = functionsGenerated.size)
    session.fsSystem.stats.currentCallbacks.dec(functionsGenerated.size)
    page.fileUploadCallbacks --= functionsFileUploadGenerated
    session.fsSystem.stats.event(StatEvent.GC_FILE_UPLOAD, n = functionsFileUploadGenerated.size)
    session.fsSystem.stats.currentFileUploadCallbacks.dec(functionsFileUploadGenerated.size)
    page.fileDownloadCallbacks --= functionsFileDownloadGenerated
    session.fsSystem.stats.event(StatEvent.GC_FILE_DOWNLOAD, n = functionsFileDownloadGenerated.size)
    session.fsSystem.stats.currentFileDownloadCallbacks.dec(functionsFileDownloadGenerated.size)
    children.foreach(_.delete())
  }

  def callback(func: () => Js): Js = callback(JS.void, _ => func())

  def callback(arg: Js, func: String => Js, async: Boolean = true, expectReturn: Boolean = true, ignoreErrors: Boolean = false, env: Js = Js("{}")): Js = {
    logger.trace(s"CREATING CALLBACK IN CONTEXT ${fullPath}")
    session.fsSystem.gc()
    val funcId = session.nextID()
    functionsGenerated += funcId
    page.callbacks += funcId -> new FSFunc(funcId, str => func(str) & session.fsSystem.afterCallBackJs.map(_(this)).getOrElse(JS.void))
    session.fsSystem.stats.event(StatEvent.CREATE_CALLBACK)
    session.fsSystem.stats.callbacksTotal.inc()
    session.fsSystem.stats.currentCallbacks.inc()

    JS.fromString(
      session.fsSystem.beforeCallBackJs.map(js => s"""(function() {${js.cmd}})();""").getOrElse("") +
        s"window._fs.callback(${if (arg.cmd.trim == "") "''" else arg.cmd},${JS.asJsStr(page.id).cmd},${JS.asJsStr(funcId).cmd},$ignoreErrors,$async,$expectReturn,$env);"
    )
  }

  def anonymousPageURL(render: FSContext => String, name: String): String = {
    session.fsSystem.gc()
    val funcId = session.nextID()
    session.fsSystem.stats.event(StatEvent.CREATE_ANON_PAGE, additionalFields = Seq("page_name" -> name))
    session.fsSystem.stats.anonPagesTotal.inc()
    session.fsSystem.stats.currentAnonPages.inc()
    session.anonymousPages += funcId -> new FSAnonymousPage(funcId, session, render)

    s"/${session.fsSystem.FSPrefix}/anon/$funcId/$name"
  }

  def createAndRedirectToAnonymousPageJS(render: FSContext => String, name: String) =
    fsc.callback(() => JS.redirectTo(anonymousPageURL(render, name)))

  def fileDownloadAutodetectContentType(fileName: String, download: () => Array[Byte]): String =
    fileDownload(fileName, Files.probeContentType(Path.of(fileName)), download)

  def fileDownload(fileName: String, contentType: String, download: () => Array[Byte]): String = {
    session.fsSystem.gc()
    val funcId = session.nextID()
    functionsFileDownloadGenerated += funcId
    page.fileDownloadCallbacks += funcId -> new FSFileDownload(funcId, contentType, download)
    session.fsSystem.stats.event(StatEvent.CREATE_FILE_DOWNLOAD, additionalFields = Seq("file_name" -> fileName, "content_type" -> contentType))
    session.fsSystem.stats.fileDownloadCallabacksTotal.inc()
    session.fsSystem.stats.currentFileDownloadCallbacks.inc()
    s"/${session.fsSystem.FSPrefix}/file-download/${page.id}/$funcId/${URLEncoder.encode(fileName, "UTF-8")}"
  }

  def fileUploadActionUrl(func: Seq[FSUploadedFile] => Js): String = {
    session.fsSystem.gc()
    val funcId = session.nextID()
    functionsFileUploadGenerated += funcId
    page.fileUploadCallbacks += (funcId -> new FSFileUpload(funcId, func))
    session.fsSystem.stats.event(StatEvent.CREATE_FILE_UPLOAD)
    session.fsSystem.stats.fileUploadCallbacksTotal.inc()
    session.fsSystem.stats.currentFileUploadCallbacks.inc()
    s"/${session.fsSystem.FSPrefix}/file-upload/${page.id}/$funcId"
  }

  def exec(func: () => Js, async: Boolean = true): Js = callback(JS("''"), _ => func(), async)

  def fsPageScript(openWSSessionAtStart: Boolean = false)(implicit fsc: FSContext): Js = page.fsPageScript(openWSSessionAtStart)
}
