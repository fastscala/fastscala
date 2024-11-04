package com.fastscala.core

import com.fastscala.js.Js
import com.fastscala.server._
import com.fastscala.stats.{FSStats, StatEvent}
import com.fastscala.utils.{IdGen, Missing}
import com.typesafe.config.ConfigFactory
import io.circe.{Decoder, Json}
import it.unimi.dsi.util.XoRoShiRo128PlusRandom
import org.eclipse.jetty.http._
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.{Request, Response => JettyServerResponse}
import org.eclipse.jetty.util.Callback
import org.eclipse.jetty.websocket.api.Callback.Completable
import org.eclipse.jetty.websocket.api.Session
import org.slf4j.LoggerFactory

import java.net.URLEncoder
import java.nio.file.{Files, Path}
import java.util.Collections
import scala.jdk.CollectionConverters.{IterableHasAsScala, MapHasAsScala}

class FSFunc(
              val id: String
              , val func: String => Js
              , var keepAliveAt: Long = System.currentTimeMillis()
              , val debugLbl: Option[String] = None
            )(implicit val fsc: FSContext) {

  def fullPath = fsc.fullPath + " => " + debugLbl.getOrElse("anon func")

  def allKeepAlivesIterable: Iterable[Long] = Iterable(keepAliveAt)

  def keepAlive(): Unit = {
    keepAliveAt = System.currentTimeMillis()
    fsc.page.keepAlive()
  }
}

class FSFileUpload(
                    val id: String
                    , val func: Seq[FSUploadedFile] => Js
                    , var keepAliveAt: Long = System.currentTimeMillis()
                  )(implicit val fsc: FSContext) {

  def allKeepAlivesIterable: Iterable[Long] = Iterable(keepAliveAt)

  def keepAlive(): Unit = {
    keepAliveAt = System.currentTimeMillis()
    fsc.page.keepAlive()
  }
}

class FSFileDownload(
                      val id: String
                      , val contentType: String
                      , val func: () => Array[Byte]
                      , var keepAliveAt: Long = System.currentTimeMillis()
                      , val debugLbl: Option[String] = None
                    )(implicit val fsc: FSContext) {

  def allKeepAlivesIterable: Iterable[Long] = Iterable(keepAliveAt)

  def keepAlive(): Unit = {
    keepAliveAt = System.currentTimeMillis()
    fsc.page.keepAlive()
  }
}

object FSContext {
  val logger = LoggerFactory.getLogger(getClass.getName)
}

class FSContext(
                 val session: FSSession
                 , val page: FSPage
                 , val parentFSContext: Option[FSContext] = None
                 , val onPageUnload: () => Js = () => Js.void
                 , val debugLbl: Option[String] = None
               ) extends FSHasSession {

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

  def fullPath = Iterator.unfold(Option(this))(_.map(ctx => (ctx, ctx.parentFSContext))).toSeq.reverse.map(_.debugLbl.getOrElse("anon")).mkString(" => ")

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

  def createNewChildContextAndGCExistingOne(key: AnyRef, debugLabel: Option[String] = None): FSContext = {
    page.key2FSContext.get(key).foreach(existing => {
      children -= existing
      existing.delete()
    })
    val newContext = new FSContext(session, page, Some(this), debugLbl = debugLabel)
    page.key2FSContext(key) = newContext
    logger.debug(s"Creating context ${newContext.fullPath}")
    children += page.key2FSContext(key)
    page.key2FSContext(key)
  }

  def delete(): Unit = {
    logger.debug(s"Delete context $fullPath")
    parentFSContext.foreach(_.children -= this)
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

  def callback(func: () => Js): Js = callback(Js.void, _ => func())

  def callback(
                arg: Js,
                func: String => Js,
                async: Boolean = true,
                expectReturn: Boolean = true,
                ignoreErrors: Boolean = false
              ): Js = {
    session.fsSystem.gc()
    val funcId = session.nextID()
    functionsGenerated += funcId
    page.callbacks += funcId -> new FSFunc(funcId, str => func(str) & session.fsSystem.afterCallBackJs.map(_(this)).getOrElse(Js.void))
    session.fsSystem.stats.event(StatEvent.CREATE_CALLBACK)
    session.fsSystem.stats.callbacksTotal.inc()
    session.fsSystem.stats.currentCallbacks.inc()

    Js.fromString(
      session.fsSystem.beforeCallBackJs.map(js => s"""(function() {${js.cmd}})();""").getOrElse("") +
        s"window._fs.callback(${if (arg.cmd.trim == "") "''" else arg.cmd},${Js.asJsStr(page.id).cmd},${Js.asJsStr(funcId).cmd},$ignoreErrors,$async,$expectReturn);"
    )
  }

  def callbackJSON(
                    arg: Js,
                    func: Json => Js,
                    async: Boolean = true,
                    expectReturn: Boolean = true,
                    ignoreErrors: Boolean = false
                  ): Js = {
    session.fsSystem.gc()
    val funcId = session.nextID()
    functionsGenerated += funcId
    // FSStats.event(StatEvent.CREATE_CALLBACK, "callback_type" -> "json")
    page.callbacks += funcId -> new FSFunc(funcId, str => {
      io.circe.parser.parse(str) match {
        case Left(value) => throw new Exception(s"Failed to parse JSON: ${value.getMessage()}")
        case Right(json) => func(json) & session.fsSystem.afterCallBackJs.map(_(this)).getOrElse(Js.void)
      }
    })
    session.fsSystem.stats.event(StatEvent.CREATE_CALLBACK)
    session.fsSystem.stats.callbacksTotal.inc()
    session.fsSystem.stats.currentCallbacks.inc()

    Js.fromString(
      session.fsSystem.beforeCallBackJs.map(js => s"""(function() {${js.cmd}})();""").getOrElse("") +
        s"window._fs.callback(${if (arg.cmd.trim == "") "''" else s"JSON.stringify(${arg.cmd})"},${Js.asJsStr(page.id).cmd},${Js.asJsStr(funcId).cmd},$ignoreErrors,$async,$expectReturn);"
    )
  }

  def callbackJSONDecoded[A: Decoder](
                                       arg: Js,
                                       func: A => Js,
                                       async: Boolean = true,
                                       expectReturn: Boolean = true,
                                       ignoreErrors: Boolean = false
                                     ): Js = {
    session.fsSystem.gc()
    val funcId = session.nextID()
    functionsGenerated += funcId
    // FSStats.event(StatEvent.CREATE_CALLBACK, "callback_type" -> "json_decoded")
    page.callbacks += funcId -> new FSFunc(funcId, str => {
      io.circe.parser.decode(str) match {
        case Left(value) => throw new Exception(s"Failed to parse JSON \"$str\": ${value.getMessage()}")
        case Right(decoded) => func(decoded) & session.fsSystem.afterCallBackJs.map(_(this)).getOrElse(Js.void)
      }
    })
    session.fsSystem.stats.event(StatEvent.CREATE_CALLBACK)
    session.fsSystem.stats.callbacksTotal.inc()
    session.fsSystem.stats.currentCallbacks.inc()

    Js.fromString(
      session.fsSystem.beforeCallBackJs.map(js => s"""(function() {${js.cmd}})();""").getOrElse("") +
        s"window._fs.callback(${if (arg.cmd.trim == "") "''" else s"JSON.stringify(${arg.cmd})"},${Js.asJsStr(page.id).cmd},${Js.asJsStr(funcId).cmd},$ignoreErrors,$async,$expectReturn);"
    )
  }

  def anonymousPageURL[E <: FSXmlEnv : FSXmlSupport](
                                                      render: FSContext => E#NodeSeq
                                                      , name: String
                                                    ): String = {
    session.fsSystem.gc()
    val funcId = session.nextID()
    session.fsSystem.stats.event(StatEvent.CREATE_ANON_PAGE, additionalFields = Seq("page_name" -> name))
    session.fsSystem.stats.anonPagesTotal.inc()
    session.fsSystem.stats.currentAnonPages.inc()
    session.anonymousPages += funcId -> new FSAnonymousPage(funcId, session, render)

    s"/${session.fsSystem.FSPrefix}/anon/$funcId/$name"
  }

  def createAndRedirectToAnonymousPageJS[E <: FSXmlEnv : FSXmlSupport](render: FSContext => E#NodeSeq, name: String)(implicit fsc: FSContext) =
    fsc.callback(() => Js.redirectTo(anonymousPageURL(render, name)))

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

  def exec(func: () => Js, async: Boolean = true): Js = callback(Js("''"), _ => func(), async)

  def fsPageScript(openWSSessionAtStart: Boolean = false)(implicit fsc: FSContext): Js = page.fsPageScript(openWSSessionAtStart)
}

class FSUploadedFile(
                      val name: String,
                      val submittedFileName: String,
                      val contentType: String,
                      val content: Array[Byte]
                    )

object FSPage {
  val logger = LoggerFactory.getLogger(getClass.getName)
}

class FSPage(
              val id: String
              , val session: FSSession
              , val req: Request
              , val createdAt: Long = System.currentTimeMillis()
              , val onPageUnload: () => Js = () => Js.void
              , var keepAliveAt: Long = System.currentTimeMillis()
              , var autoKeepAliveAt: Long = System.currentTimeMillis()
              , var wsSession: Option[Session] = None
              , var wsQueue: List[Js] = Nil
              , var wsLock: AnyRef = new AnyRef
              , val debugLbl: Option[String] = None
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
  val fileUploadCallbacks = collection.mutable.Map[String, FSFileUpload]()
  val fileDownloadCallbacks = collection.mutable.Map[String, FSFileDownload]()

  val rootFSContext = new FSContext(session, this, onPageUnload = onPageUnload, debugLbl = Some("page_root_context"))

  def deleteOlderThan(ts: Long): Unit = {
    val currentCallbacks = callbacks.toVector
    val callbacksToRemove = currentCallbacks.filter(_._2.keepAliveAt < ts)
    callbacks --= callbacksToRemove.map(_._1)
    callbacksToRemove.foreach({
      case (_, f) => f.fsc.functionsGenerated -= f.id
    })
    session.fsSystem.stats.currentCallbacks.dec(callbacksToRemove.size)

    //    session.fsSystem.stats.event(StatEvent.GC_CALLBACK, n = functionsToRemove.size)
    // logger.info(s"Removed ${functionsToRemove.size} functions")

    val currentFileUploadCallbacks = fileUploadCallbacks.toVector
    val fileUploadCallbacksToRemove = currentFileUploadCallbacks.filter(_._2.keepAliveAt < ts)
    fileUploadCallbacks --= fileUploadCallbacksToRemove.map(_._1)
    fileUploadCallbacksToRemove.foreach({
      case (_, f) => f.fsc.functionsFileUploadGenerated -= f.id
    })
    session.fsSystem.stats.currentFileUploadCallbacks.dec(fileUploadCallbacksToRemove.size)

    //    session.fsSystem.stats.event(StatEvent.GC_FILE_UPLOAD, n = fileUploadCallbacksToRemove.size)
    // logger.info(s"Removed ${functionsFileUploadToRemove.size} functions")

    val currentFileDownloadCallbacks = fileDownloadCallbacks.toVector
    val fileDownloadCallbacksToRemove = currentFileDownloadCallbacks.filter(_._2.keepAliveAt < ts)
    fileDownloadCallbacks --= fileDownloadCallbacksToRemove.map(_._1)
    fileDownloadCallbacksToRemove.foreach({
      case (_, f) => f.fsc.functionsFileDownloadGenerated -= f.id
    })
    session.fsSystem.stats.currentFileDownloadCallbacks.dec(fileDownloadCallbacksToRemove.size)

    //    session.fsSystem.stats.event(StatEvent.GC_FILE_DOWNLOAD, n = fileDownloadCallbacksToRemove.size)
    // logger.info(s"Removed ${functionsFileDownloadToRemove.size} functions")
  }

  def delete(): Unit = {
    session.pages -= this.id
    session.fsSystem.stats.currentPages.dec()
    session.fsSystem.stats.currentFileDownloadCallbacks.dec(fileDownloadCallbacks.size)
    session.fsSystem.stats.currentFileUploadCallbacks.dec(fileUploadCallbacks.size)
    session.fsSystem.stats.currentCallbacks.dec(callbacks.size)
  }

  def fsPageScript(openWSSessionAtStart: Boolean = false)(implicit fsc: FSContext): Js = {
    Js {
      s"""window._fs = {
         |  sessionId: ${Js.asJsStr(session.id).cmd},
         |  pageId: ${Js.asJsStr(id).cmd},
         |  callback: function(arg, pageId, funcId, ignoreErrors, async, expectReturn) {
         |    const xhr = new XMLHttpRequest();
         |    xhr.open("POST", "/${session.fsSystem.FSPrefix}/cb/"+pageId+"/"+funcId+"?time=" + new Date().getTime() + (ignoreErrors ? "&ignore_errors=true" : ""), async);
         |    xhr.setRequestHeader("Content-type", "text/plain;charset=utf-8");
         |    if (expectReturn) {
         |      xhr.onload = function() { try {eval(this.responseText);} catch(err) { console.log(err.message); console.log('While runnning the code:\\n' + this.responseText); } };
         |    }
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
         |      window._fs.ws = new WebSocket(location.origin.replace(/^http/, 'ws') + "/${session.fsSystem.FSPrefix}/ws?sessionId=${URLEncoder.encode(fsc.session.id, "UTF-8")}&pageId=${URLEncoder.encode(fsc.page.id, "UTF-8")}", "fs");
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
         |${if (openWSSessionAtStart) initWebSocket().cmd else Js.void.cmd}
         |${if (periodicKeepAliveEnabled) setupKeepAlive().cmd else Js.void.cmd}
         |""".stripMargin
    }
  }

  def isDefunct_? = periodicKeepAliveEnabled && (System.currentTimeMillis() - autoKeepAliveAt) > periodicKeepAliveDefunctAfter

  def setupKeepAlive(): Js = {

    Js(s"""function sendKeepAlive() {window._fs.keepAlive(${Js.asJsStr(id).cmd});setTimeout(sendKeepAlive, ${periodicKeepAlivePeriod});};sendKeepAlive();""")
  }

  def initWebSocket() = Js("window._fs.initWebSocket();")
}

object FSSession {
  val logger = LoggerFactory.getLogger(getClass.getName)
}

abstract class FSSessionVar[T](default: => T) {

  def apply()(implicit hasSession: FSHasSession): T = hasSession.session.getDataOpt(this).getOrElse({
    hasSession.session.setData(this, default)
    apply()
  })

  def update(value: T)(implicit hasSession: FSHasSession): Unit = hasSession.session.setData(this, value)
}

abstract class FSSessionVarOpt[T]() {

  def apply()(implicit hasSession: FSHasSession): Option[T] = hasSession.session.getDataOpt[T](this)

  def update(value: T)(implicit hasSession: FSHasSession): Unit = hasSession.session.setData(this, value)

  def setOrClear(valueOpt: Option[T])(implicit hasSession: FSHasSession): Unit = valueOpt match {
    case Some(value) => update(value)
    case None => clear()
  }

  def clear()(implicit hasSession: FSHasSession): Unit = hasSession.session.clear(this)
}

class FSAnonymousPage[E <: FSXmlEnv : FSXmlSupport](
                                                     val id: String
                                                     , val session: FSSession
                                                     , val render: FSContext => E#NodeSeq
                                                     , val createdAt: Long = System.currentTimeMillis()
                                                     , var keepAliveAt: Long = System.currentTimeMillis()
                                                     , var debugLbl: Option[String] = None
                                                   ) extends FSHasSession {

  def renderAsString()(implicit fsc: FSContext): String = implicitly[FSXmlSupport[E]].render(render(fsc))

  def keepAlive(): Unit = {
    keepAliveAt = System.currentTimeMillis()
    session.keepAlive()
  }

  def allKeepAlivesIterable: Iterable[Long] = Iterable(keepAliveAt)

  def onPageUnload(): Js = {
    // session.anonymousPages.remove(id)
    Js.void
  }
}

trait FSHasSession {
  def session: FSSession
}

class FSSession(
                 val id: String
                 , val fsSystem: FSSystem
                 , data: collection.mutable.Map[Any, Any] = collection.mutable.Map.empty[Any, Any]
                 , val createdAt: Long = System.currentTimeMillis()
                 , var keepAliveAt: Long = System.currentTimeMillis()
                 , val debugLbl: Option[String] = None
               ) extends FSHasSession {

  val config = ConfigFactory.load()

  def useRandomIds: Boolean = Option(config.getBoolean("com.fastscala.core.random-ids")).getOrElse(false)

  val logger = LoggerFactory.getLogger(getClass.getName)

  private var idSeq = 0L
  private lazy val xoRoShiRo128PlusRandom = new XoRoShiRo128PlusRandom()

  private implicit def __fsContextOpt: Option[FSContext] = None

  private implicit def __fsPageOpt: Option[FSPage] = None

  private implicit def __fsSessionOpt: Option[FSSession] = Some(this)

  private implicit def __fsSystem: FSSystem = fsSystem

  def nextID(prefix: String = ""): String = {
    if (useRandomIds) {
      prefix + java.lang.Long.toHexString(xoRoShiRo128PlusRandom.nextLong())
    } else {
      session.synchronized {
        session.idSeq += 1
        prefix + session.idSeq.toString
      }
    }
  }

  override def session: FSSession = this

  def keepAlive(): Unit = {
    keepAliveAt = System.currentTimeMillis()
  }

  def allKeepAlivesIterable: Iterable[Long] = pages.values.flatMap(_.allKeepAlivesIterable) ++ Iterable(keepAliveAt)

  //  override def finalize(): Unit = {
  //    super.finalize()
  //    FSSession.logger.trace(s"GC SESSION lived ${((System.currentTimeMillis() - createdAt) / 1000d).formatted("%.2f")}s session_id=$id #pages=${pages.keys.size} #anonymous_pages=${anonymousPages.keys.size}, evt_type=gc_session")
  //  }

  val pages = collection.mutable.Map[String, FSPage]()
  val anonymousPages = collection.mutable.Map[String, FSAnonymousPage[_]]()

  def nPages(): Int = pages.size

  def getData[T](key: Any): T = getDataOpt(key).get

  def getDataOpt[T](key: Any): Option[T] = data.get(key).map(_.asInstanceOf[T])

  def setData[T](key: Any, value: T): Unit = data(key) = value

  def clear[T](key: Any): Unit = data.remove(key)

  def createPage[T](
                     code: FSContext => T,
                     debugLbl: Option[String] = None,
                     onPageUnload: () => Js = () => Js.void
                   )(implicit req: Request): T = try {
    session.fsSystem.gc()
    val copy = new Request.Wrapper(req)
    val page = new FSPage(nextID(), this, copy, onPageUnload = onPageUnload, debugLbl = debugLbl)
    if (logger.isTraceEnabled) logger.trace(s"Created page ${page.id}")
    fsSystem.stats.event(StatEvent.CREATE_PAGE)
    session.fsSystem.stats.pagesTotal.inc()
    session.fsSystem.stats.currentPages.inc()
    this.synchronized {
      pages += (page.id -> page)
    }
    // FSSession.logger.trace(s"Created page: page_id=${page.id}, evt_type=create_page")
    code(page.rootFSContext)
  } catch {
    case ex: Exception =>
      ex.printStackTrace()
      throw ex
  }

  def deleteOlderThan(ts: Long): Unit = {
    val currentPages = pages.toVector
    val pagesToRemove = currentPages.filter(_._2.keepAliveAt < ts)
    pagesToRemove.foreach(_._2.delete())
    fsSystem.stats.currentPages.dec(pagesToRemove.size)

    //    fsSystem.stats.event(StatEvent.GC_PAGE, n = pagesToRemove.size)
    // logger.info(s"Removed ${pagesToRemove.size} pages")

    val currentAnonymousPages = anonymousPages.toVector
    val anonymousPagesToRemove = currentAnonymousPages.filter(_._2.keepAliveAt < ts)
    anonymousPages --= anonymousPagesToRemove.map(_._1)
    fsSystem.stats.currentAnonPages.dec(anonymousPagesToRemove.size)
    //    fsSystem.stats.event(StatEvent.GC_ANON_PAGE, n = anonymousPagesToRemove.size)
    // logger.info(s"Removed ${pagesToRemove.size} anonymous pages")

    pages.values.foreach(_.deleteOlderThan(ts))
  }

  def delete(): Unit = {
    fsSystem.sessions -= this.id
    fsSystem.stats.currentSessions.dec()
    pages.foreach(_._2.delete())
  }
}

class FSSystem(
                val beforeCallBackJs: Option[Js] = None
                , val afterCallBackJs: Option[FSContext => Js] = None
                , val appName: String = "app"
                , val stats: FSStats = new FSStats()
              ) extends RoutingHandlerNoSessionHelper {

  val config = ConfigFactory.load()

  def debug: Boolean = config.getBoolean("com.fastscala.core.debug")

  val logger = LoggerFactory.getLogger(getClass.getName)
  val sessions: collection.mutable.Map[String, FSSession] = collection.mutable.Map[String, FSSession]()

  // private implicit def __fsContextOpt: Option[FSContext] = None
  // private implicit def __fsPageOpt: Option[FSPage] = None
  // private implicit def __fsSessionOpt: Option[FSSession] = None
  private implicit def __fsSystem: FSSystem = this

  def FSSessionIdCookieName = "fssid"

  val FSPrefix = "fs"

  def inSession[T](inSession: FSSession => T)(implicit req: Request): Option[(List[HttpCookie], T)] = {
    val cookies = Option(Request.getCookies(req)).getOrElse(Collections.emptyList).asScala
    cookies.filter(_.getName == FSSessionIdCookieName).map(_.getValue).flatMap(sessions.get(_)).headOption match {
      case Some(session) => Option((Nil, inSession(session)))
      case None if !req.getHttpURI.getPath.startsWith("/" + FSPrefix + "/ws") =>
        gc()
        val id = IdGen.secureId()
        val session = new FSSession(id, this)
        implicit val __fsContextOpt: Option[FSContext] = None
        implicit val __fsPageOpt: Option[FSPage] = None
        implicit val __fsSessionOpt: Option[FSSession] = Some(session)
        stats.event(StatEvent.CREATE_SESSION)
        session.fsSystem.stats.sessionsTotal.inc()
        session.fsSystem.stats.currentSessions.inc()
        this.synchronized(sessions.put(session.id, session))
        FSSession.logger.info(s"Created session: session_id=${session.id}, evt_type=create_session")
        Option((List(HttpCookie.build(FSSessionIdCookieName, session.id).path("/").build()), inSession(session)))
      case None => None
    }
  }

  def handleCallbackException(ex: Throwable): Js = {
    ex.printStackTrace()
    stats.callbackErrorsTotal.inc()
    if (__fsSystem.debug) {
      Js.alert(s"Internal error: ${ex.getMessage} (showing because in debug mode)")
    } else {
      Js.alert(s"Internal error")
    }
  }

  def handleFileUploadCallbackException(ex: Throwable): Js = {
    ex.printStackTrace()
    stats.fileUploadCallbackErrorsTotal.inc()
    if (__fsSystem.debug) {
      Js.alert(s"Internal error: ${ex.getMessage} (showing because in debug mode)")
    } else {
      Js.alert(s"Internal error")
    }
  }

  def handleFileDownloadCallbackException(ex: Throwable): Response = {
    ex.printStackTrace()
    stats.fileDownloadCallbackErrorsTotal.inc()
    if (__fsSystem.debug) {
      ServerError.InternalServerError(s"Internal error: ${ex.getMessage} (showing because in debug mode)")
    } else {
      ServerError.InternalServerError
    }
  }

  override def handlerNoSession(response: JettyServerResponse, callback: Callback)(implicit req: Request): Option[Response] = {
    val cookies = Option(Request.getCookies(req)).getOrElse(Collections.emptyList).asScala
    import RoutingHandlerHelper._

    val sessionIdOpt = cookies.find(_.getName == FSSessionIdCookieName).map(_.getValue).orElse(
      Option(Request.getParameters(req).getValue(FSSessionIdCookieName)).filter(_ != "")
    )
    val sessionOpt = sessionIdOpt.flatMap(sessionId => sessions.get(sessionId))

    Some(req).collect {
      // Keep alive:
      case Post(FSPrefix, "ka", pageId) =>
        sessionOpt.map(implicit session => {
          session.pages.get(pageId).map(implicit page => {
            stats.keepAliveInvocationsTotal.inc()
            page.autoKeepAliveAt = System.currentTimeMillis()
            Ok.js(Js.void)
          }).getOrElse(Ok.js(onKeepAliveNotFound(Missing.Page, sessionId = sessionIdOpt, pageId = pageId, session = Some(session), page = None)))
        }).getOrElse(Ok.js(onKeepAliveNotFound(Missing.Session, sessionId = sessionIdOpt, pageId = pageId, session = sessionOpt, page = None)))

      // Callback invocation:
      case Post(FSPrefix, "cb", pageId, funcId) =>
        sessionOpt.map(implicit session => {
          session.pages.get(pageId).map(implicit page => {
            page.callbacks.get(funcId).map(implicit fsFunc => {
              fsFunc.keepAlive()
              val start = System.currentTimeMillis()
              stats.callbackInvocationsTotal.inc()
              stats.event(StatEvent.USE_CALLBACK, additionalFields = Seq("func_name" -> fsFunc.fullPath))

              Content.Source.asStringAsync(req, Request.getCharset(req)).whenComplete((arg, failure) => {
                val rslt: Js = Option(failure) match {
                  case Some(failure) =>
                    handleCallbackException(failure)
                  case None =>
                    val start = System.currentTimeMillis()
                    try {
                      stats.callbacksInProcessing.inc()
                      if (logger.isTraceEnabled) logger.trace(s"Running callback on thread ${Thread.currentThread()}...")
                      val rslt = fsFunc.func(arg)
                      if (logger.isTraceEnabled) logger.trace(s"Finished in ${System.currentTimeMillis() - start}ms (thread ${Thread.currentThread()}) with result JS with ${rslt.cmd.length} chars")
                      rslt
                    } catch {
                      case trowable =>
                        if (logger.isTraceEnabled) logger.trace(s"Finished with EXCEPTION in ${System.currentTimeMillis() - start}ms (thread ${Thread.currentThread()}): $trowable")
                        handleCallbackException(trowable)
                    } finally {
                      stats.callbackTimeTotal.inc(io.prometheus.metrics.model.snapshots.Unit.millisToSeconds(System.currentTimeMillis() - start))
                      stats.callbacksInProcessing.dec()
                    }
                }
                if (logger.isTraceEnabled) logger.trace(s"Invoke func: session_id=${session.id}, page_id=$pageId, func_id=$funcId, took_ms=${System.currentTimeMillis() - start}, response_size_bytes=${rslt.cmd.getBytes.size}, evt_type=invk_func")
                Ok.js(rslt).respond(response, callback)
              })
              VoidResponse
            }).getOrElse(Ok.js(onCallbackNotFound(Missing.CallbackFunction, sessionId = sessionIdOpt, pageId = pageId, functionId = funcId, session = Some(session), page = Some(page))))
          }).getOrElse(Ok.js(onCallbackNotFound(Missing.Page, sessionId = sessionIdOpt, pageId = pageId, functionId = funcId, session = Some(session), page = None)))
        }).getOrElse(Ok.js(onCallbackNotFound(Missing.Session, sessionId = sessionIdOpt, pageId = pageId, functionId = funcId, session = sessionOpt, page = None)))

      case Post(FSPrefix, "file-upload", pageId, funcId) =>
        sessionOpt.map(implicit session => {
          session.pages.get(pageId).map(implicit page => {
            page.fileUploadCallbacks.get(funcId).map(implicit fSFileUpload => {

              fSFileUpload.keepAlive()
              val start = System.currentTimeMillis()
              stats.fileUploadCallbackInvocationsTotal.inc()

              val contentType = req.getHeaders.get(HttpHeader.CONTENT_TYPE)
              if (MimeTypes.Type.MULTIPART_FORM_DATA == MimeTypes.getBaseType(contentType)) {
                // Extract the multipart boundary.
                val boundary = MultiPart.extractBoundary(contentType)
                // Create and configure the multipart parser.
                val parser = new MultiPartFormData.Parser(boundary)
                // By default, uploaded files are stored in this directory, to
                // avoid to read the file content (which can be large) in memory.
                parser.setFilesDirectory(Path.of(System.getProperty("java.io.tmpdir")))
                // Convert the request content into parts.
                parser.parse(req).whenComplete((parts, failure) => {
                  val rslt: Js = Option(failure) match {
                    case Some(failure) =>
                      handleFileUploadCallbackException(failure)
                    case None =>
                      stats.fileUploadCallbacksInProcessing.inc()
                      fSFileUpload.func(parts.asScala.map(part => try {
                        new FSUploadedFile(
                          name = part.getName,
                          submittedFileName = part.getFileName,
                          contentType = part.getHeaders.get(HttpHeader.CONTENT_TYPE),
                          content = Content.Source.asByteBuffer(part.getContentSource).array
                        )
                      } catch {
                        case ex: Throwable =>
                          handleFileUploadCallbackException(ex)
                          throw ex
                      } finally {
                        stats.fileUploadCallbackTimeTotal.inc(io.prometheus.metrics.model.snapshots.Unit.millisToSeconds(System.currentTimeMillis() - start))
                        stats.fileUploadCallbacksInProcessing.dec()
                        try {
                          part.close()
                        } finally {
                          part.delete()
                        }
                      }).toSeq)
                  }
                  Ok.js(rslt).respond(response, callback)
                })
                VoidResponse
              } else {
                Ok.js(Js.alert("Not multipart form data"))
              }
            }).getOrElse(Ok.js(onFileUploadNotFound(Missing.FileUploadFunction, sessionId = sessionIdOpt, pageId = pageId, functionId = funcId, session = Some(session), page = Some(page))))
          }).getOrElse(Ok.js(onFileUploadNotFound(Missing.Page, sessionId = sessionIdOpt, pageId = pageId, functionId = funcId, session = Some(session), page = None)))
        }).getOrElse(Ok.js(onFileUploadNotFound(Missing.Session, sessionId = sessionIdOpt, pageId = pageId, functionId = funcId, session = sessionOpt, page = None)))

      // File downloads:
      case Get(FSPrefix, "file-download", pageId, funcId, _) =>
        sessionOpt.map(implicit session => {
          session.pages.get(pageId).map(implicit page => {
            page.fileDownloadCallbacks.get(funcId).map(implicit fSFileDownload => {

              fSFileDownload.keepAlive()
              val start = System.currentTimeMillis()
              stats.fileDownloadCallbackInvocationsTotal.inc()

              try {
                stats.fileDownloadCallbacksInProcessing.inc()
                val bytes = fSFileDownload.func()
                Ok.binaryWithContentType(bytes, fSFileDownload.contentType)
              } catch {
                case ex: Exception => handleFileDownloadCallbackException(ex)
              } finally {
                stats.fileDownloadCallbackTimeTotal.inc(io.prometheus.metrics.model.snapshots.Unit.millisToSeconds(System.currentTimeMillis() - start))
                stats.fileDownloadCallbacksInProcessing.dec()
              }
            }).getOrElse(onFileDownloadNotFound(Missing.FileDownloadFunction, sessionId = sessionIdOpt, pageId = pageId, functionId = funcId, session = Some(session), page = Some(page)))
          }).getOrElse(onFileDownloadNotFound(Missing.Page, sessionId = sessionIdOpt, pageId = pageId, functionId = funcId, session = Some(session), page = None))
        }).getOrElse(onFileDownloadNotFound(Missing.Session, sessionId = sessionIdOpt, pageId = pageId, functionId = funcId, session = sessionOpt, page = None))

      // Anonymous pages:
      case Get(FSPrefix, "anon", anonymousPageId, _) =>

        sessionOpt.map(implicit session => {
          session.anonymousPages.get(anonymousPageId).map(implicit anonymousPage => {
            anonymousPage.keepAlive()
            val nodeSeq = session.createPage(implicit fsc => anonymousPage.renderAsString(), onPageUnload = () => {
              anonymousPage.onPageUnload()
            }, debugLbl = Some(s"page for anon page ${anonymousPage.debugLbl.getOrElse(s"with id ${anonymousPage.id}")}"))
            Ok.html(nodeSeq)
          }).getOrElse(onAnonymousPageNotFound(Missing.AnonPage, sessionId = sessionIdOpt, anonPageId = anonymousPageId, session = Some(session), page = None))
        }).getOrElse(onAnonymousPageNotFound(Missing.Session, sessionId = sessionIdOpt, anonPageId = anonymousPageId, session = sessionOpt, page = None))
    }
  }

  def onKeepAliveNotFound(
                           missing: Missing.Value,
                           sessionId: Option[String],
                           pageId: String,
                           session: Option[FSSession],
                           page: Option[FSPage],
                         )(implicit req: Request): Js = {
    missing.updateStats()
    Js.void
  }

  def onCallbackNotFound(
                          missing: Missing.Value,
                          sessionId: Option[String],
                          pageId: String,
                          functionId: String,
                          session: Option[FSSession],
                          page: Option[FSPage],
                        )(implicit req: Request): Js = {
    missing.updateStats()
    if (Option(Request.getParameters(req).getValue("ignore_errors")).map(_ == "true").getOrElse(false)) Js.void
    else Js.confirm(s"Page has expired, please reload", Js.reload())
  }

  def onFileUploadNotFound(
                            missing: Missing.Value,
                            sessionId: Option[String],
                            pageId: String,
                            functionId: String,
                            session: Option[FSSession],
                            page: Option[FSPage],
                          )(implicit req: Request): Js = {
    missing.updateStats()
    if (Option(Request.getParameters(req).getValue("ignore_errors")).map(_ == "true").getOrElse(false)) Js.void
    else Js.confirm(s"Page has expired, please reload", Js.reload())
  }

  def onFileDownloadNotFound(
                              missing: Missing.Value,
                              sessionId: Option[String],
                              pageId: String,
                              functionId: String,
                              session: Option[FSSession],
                              page: Option[FSPage],
                            )(implicit req: Request): Response = {
    missing.updateStats()
    Redirect.temporaryRedirect("/")
  }

  def onAnonymousPageNotFound(
                               missing: Missing.Value,
                               sessionId: Option[String],
                               anonPageId: String,
                               session: Option[FSSession],
                               page: Option[FSPage],
                             )(implicit req: Request): Response = {
    missing.updateStats()
    Redirect.temporaryRedirect("/")
  }

  def onWebsocketNotFound(
                           missing: Missing.Value,
                           sessionId: String,
                           pageId: String,
                           session: Option[FSSession],
                           page: Option[FSPage],
                         )(implicit wsSession: Session): Js = {
    missing.updateStats()
    if (wsSession.getUpgradeRequest.getParameterMap.asScala.get("ignore_errors").flatMap(_.asScala.headOption).map(_ == "true").getOrElse(false)) Js.void
    else Js.confirm(s"Page has expired, please reload", Js.reload())
  }

  def transformCallbackResponse(js: Js)(implicit fsc: FSContext): Js = js

  def transformFileUploadCallbackResponse(js: Js)(implicit fsc: FSContext): Js = js

  def allKeepAlivesIterable: Iterable[Long] = sessions.values.flatMap(_.allKeepAlivesIterable)

  def gc(): Unit = {
    val minFreeSpacePercent = config.getDouble("com.fastscala.core.gc.run-when-less-than-mem-free-percent")
    if (
      (Runtime.getRuntime.freeMemory().toDouble / Runtime.getRuntime.totalMemory() * 100) < minFreeSpacePercent
    ) synchronized {
      if (logger.isTraceEnabled) logger.trace(s"Less than ${minFreeSpacePercent.formatted("%.2f%%")} space available, freeing up space...")
      freeUpSpace()
      System.gc()
      gc()
    }
  }

  def freeUpSpace(): Unit = {
    stats.gcRunsTotal.inc()
    val start = System.currentTimeMillis()
    if (logger.isTraceEnabled) logger.trace(s"#Sessions: ${sessions.size} #Pages: ${sessions.map(_._2.pages.size).sum} #Funcs: ${sessions.map(_._2.pages.map(_._2.callbacks.size).sum).sum}")
    val allKeepAlives = allKeepAlivesIterable.toVector.sorted
    if (logger.isTraceEnabled) logger.trace(s"Found ${allKeepAlives.size} keep alives")
    val deleteOlderThanTs: Long = allKeepAlives.drop(allKeepAlives.size / 2).headOption.getOrElse(0L)
    if (logger.isTraceEnabled) logger.trace(s"Removing everything with keepalive older than ${System.currentTimeMillis() - deleteOlderThanTs}ms")
    deleteOlderThan(deleteOlderThanTs)
    stats.gcTimeTotal.inc(100000)
    stats.gcTimeTotal.inc(io.prometheus.metrics.model.snapshots.Unit.millisToSeconds(System.currentTimeMillis() - start))
  }

  val gcLock = new Object

  def deleteOlderThan(keepAliveOlderThan: Long): Unit = gcLock.synchronized {
    val currentSessions = sessions.toVector
    val sessionsToRemove = currentSessions.filter(_._2.keepAliveAt < keepAliveOlderThan)
    sessionsToRemove.foreach(_._2.delete())
    if (logger.isTraceEnabled) logger.trace(s"Removed ${sessionsToRemove.size} sessions: ${sessionsToRemove.map(_._1).mkString(", ")}")
    sessions.values.foreach(_.deleteOlderThan(keepAliveOlderThan))
  }
}
