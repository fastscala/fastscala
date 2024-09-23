package com.fastscala.core

import com.fastscala.js.Js
import com.fastscala.server._
import com.fastscala.stats.{FSStats, StatEvent}
import com.fastscala.utils.IdGen
import com.typesafe.config.ConfigFactory
import io.circe.{Decoder, Json}
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
      existing.gc()
    })
    val newContext = new FSContext(session, page, Some(this), debugLbl = debugLabel)
    page.key2FSContext(key) = newContext
    logger.debug(s"Creating context ${newContext.fullPath}")
    children += page.key2FSContext(key)
    page.key2FSContext(key)
  }

  def gc(): Unit = {
    logger.debug(s"GC context $fullPath")
    parentFSContext.foreach(_.children -= this)
    page.functions --= functionsGenerated
    session.fsSystem.stats.event(StatEvent.GC_CALLBACK, n = functionsGenerated.size)
    page.functionsFileUpload --= functionsFileUploadGenerated
    session.fsSystem.stats.event(StatEvent.GC_FILE_UPLOAD, n = functionsFileUploadGenerated.size)
    page.functionsFileDownload --= functionsFileDownloadGenerated
    session.fsSystem.stats.event(StatEvent.GC_FILE_DOWNLOAD, n = functionsFileDownloadGenerated.size)
    children.foreach(_.gc())
  }

  def callback(func: () => Js): Js = callback(Js.void, _ => func())

  def callback(
                arg: Js,
                func: String => Js,
                async: Boolean = true,
                expectReturn: Boolean = true,
                ignoreErrors: Boolean = false
              ): Js = {
    session.fsSystem.checkSpace()
    val funcId = session.nextID()
    functionsGenerated += funcId
    // FSStats.event(StatEvent.CREATE_CALLBACK, "callback_type" -> "str")
    page.functions += funcId -> new FSFunc(funcId, str => func(str) & session.fsSystem.afterCallBackJs.map(_(this)).getOrElse(Js.void))
    session.fsSystem.stats.event(StatEvent.CREATE_CALLBACK)

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
    session.fsSystem.checkSpace()
    val funcId = session.nextID()
    functionsGenerated += funcId
    // FSStats.event(StatEvent.CREATE_CALLBACK, "callback_type" -> "json")
    page.functions += funcId -> new FSFunc(funcId, str => {
      io.circe.parser.parse(str) match {
        case Left(value) => throw new Exception(s"Failed to parse JSON: ${value.getMessage()}")
        case Right(json) => func(json) & session.fsSystem.afterCallBackJs.map(_(this)).getOrElse(Js.void)
      }
    })
    session.fsSystem.stats.event(StatEvent.CREATE_CALLBACK)

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
    session.fsSystem.checkSpace()
    val funcId = session.nextID()
    functionsGenerated += funcId
    // FSStats.event(StatEvent.CREATE_CALLBACK, "callback_type" -> "json_decoded")
    page.functions += funcId -> new FSFunc(funcId, str => {
      io.circe.parser.decode(str) match {
        case Left(value) => throw new Exception(s"Failed to parse JSON \"$str\": ${value.getMessage()}")
        case Right(decoded) => func(decoded) & session.fsSystem.afterCallBackJs.map(_(this)).getOrElse(Js.void)
      }
    })
    session.fsSystem.stats.event(StatEvent.CREATE_CALLBACK)

    Js.fromString(
      session.fsSystem.beforeCallBackJs.map(js => s"""(function() {${js.cmd}})();""").getOrElse("") +
        s"window._fs.callback(${if (arg.cmd.trim == "") "''" else s"JSON.stringify(${arg.cmd})"},${Js.asJsStr(page.id).cmd},${Js.asJsStr(funcId).cmd},$ignoreErrors,$async,$expectReturn);"
    )
  }

  def anonymousPageURL[E <: FSXmlEnv : FSXmlSupport](
                                                      render: FSContext => E#NodeSeq
                                                      , name: String
                                                    ): String = {
    session.fsSystem.checkSpace()
    val funcId = session.nextID()
    session.fsSystem.stats.event(StatEvent.CREATE_ANON_PAGE, additionalFields = Seq("page_name" -> name))
    session.anonymousPages += funcId -> new FSAnonymousPage(funcId, session, render)

    s"/${session.fsSystem.FSPrefix}/anon/$funcId/$name"
  }

  def createAndRedirectToAnonymousPageJS[E <: FSXmlEnv : FSXmlSupport](render: FSContext => E#NodeSeq, name: String)(implicit fsc: FSContext) =
    fsc.callback(() => Js.redirectTo(anonymousPageURL(render, name)))

  def fileDownloadAutodetectContentType(fileName: String, download: () => Array[Byte]): String =
    fileDownload(fileName, Files.probeContentType(Path.of(fileName)), download)

  def fileDownload(fileName: String, contentType: String, download: () => Array[Byte]): String = {
    session.fsSystem.checkSpace()
    val funcId = session.nextID()
    functionsFileUploadGenerated += funcId
    page.functionsFileDownload += funcId -> new FSFileDownload(funcId, contentType, download)
    session.fsSystem.stats.event(StatEvent.CREATE_FILE_DOWNLOAD, additionalFields = Seq("file_name" -> fileName, "content_type" -> contentType))
    s"/${session.fsSystem.FSPrefix}/file-download/${page.id}/$funcId/${URLEncoder.encode(fileName, "UTF-8")}"
  }

  def fileUploadActionUrl(func: Seq[FSUploadedFile] => Js): String = {
    session.fsSystem.checkSpace()
    val funcId = session.nextID()
    functionsFileDownloadGenerated += funcId
    page.functionsFileUpload += (funcId -> new FSFileUpload(funcId, func))
    session.fsSystem.stats.event(StatEvent.CREATE_FILE_UPLOAD)
    s"/${session.fsSystem.FSPrefix}/file-upload/${page.id}/$funcId"
  }

  def exec(func: () => Js, async: Boolean = true): Js = callback(Js("''"), _ => func(), async)

  def fsPageScript(openWSSessionAtStart: Boolean = false)(implicit fsc: FSContext): Js = {
    // Js(s"""window.addEventListener("beforeunload", function(evt) {${fsc.callback(Js.void, _ => session.fsSystem.onPageUnload(), expectReturn = false, ignoreErrors = true).cmd}});""") &
    //  onPageUnload()

    Js {
      s"""window._fs = {
         |  sessionId: ${Js.asJsStr(session.id).cmd},
         |  pageId: ${Js.asJsStr(page.id).cmd},
         |  callback: function(arg, pageId, funcId, ignoreErrors, async, expectReturn) {
         |    const xhr = new XMLHttpRequest();
         |    xhr.onload = function() { eval(this.responseText); };
         |
         |    xhr.open("POST", "/${session.fsSystem.FSPrefix}/cb/"+pageId+"/"+funcId+"?time=" + new Date().getTime() + (ignoreErrors ? "&ignore_errors=true" : ""), async);
         |    xhr.setRequestHeader("Content-type", "text/plain;charset=utf-8");
         |    if (expectReturn) {
         |      xhr.onload = function() { try {eval(this.responseText);} catch(err) { console.log(err.message); console.log('While runnning the code:\\n' + this.responseText); } };
         |    }
         |    xhr.send(arg);
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
         |""".stripMargin
    }
  }

  def initWebSocket() = Js("window._fs.initWebSocket();")
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
              , var wsSession: Option[Session] = None
              , var wsQueue: List[Js] = Nil
              , var wsLock: AnyRef = new AnyRef
              , val debugLbl: Option[String] = None
            ) extends FSHasSession {


  private implicit def __fsContextOpt: Option[FSContext] = None

  private implicit def __fsPageOpt: Option[FSPage] = Some(this)

  private implicit def __fsSessionOpt: Option[FSSession] = Some(session)

  private implicit def __fsSystem: FSSystem = session.fsSystem

  //  override def finalize(): Unit = {
  //    super.finalize()
  //    FSSession.logger.trace(s"GC PAGE ${((System.currentTimeMillis() - createdAt) / 1000d).formatted("%.2f")}s session_id=$id page_id=$id #functions=${functions.keys.size} #functions_file_upload=${functionsFileUpload.keys.size} #functions_file_download=${functionsFileDownload.keys.size}, evt_type=gc_page")
  //  }

  def keepAlive(): Unit = {
    keepAliveAt = System.currentTimeMillis()
    session.keepAlive()
  }

  def allKeepAlivesIterable: Iterable[Long] =
    functions.values.map(_.keepAliveAt) ++
      functionsFileUpload.values.map(_.keepAliveAt) ++
      functionsFileDownload.values.map(_.keepAliveAt) ++
      Iterable(keepAliveAt)

  val key2FSContext = collection.mutable.Map[AnyRef, FSContext]()

  val functions = collection.mutable.Map[String, FSFunc]()
  val functionsFileUpload = collection.mutable.Map[String, FSFileUpload]()
  val functionsFileDownload = collection.mutable.Map[String, FSFileDownload]()

  val rootFSContext = new FSContext(session, this, onPageUnload = onPageUnload, debugLbl = Some("page_root_context"))

  def gc(keepAliveOlderThan: Long): Unit = {
    val currentFunctions = functions.toVector
    val functionsToRemove = currentFunctions.filter(_._2.keepAliveAt < keepAliveOlderThan)
    functions --= functionsToRemove.map(_._1)
    functionsToRemove.foreach({
      case (_, f) => f.fsc.functionsGenerated -= f.id
    })
    session.fsSystem.stats.event(StatEvent.GC_CALLBACK, n = functionsToRemove.size)
    // logger.info(s"Removed ${functionsToRemove.size} functions")

    val currentFunctionsFileUpload = functionsFileUpload.toVector
    val functionsFileUploadToRemove = currentFunctionsFileUpload.filter(_._2.keepAliveAt < keepAliveOlderThan)
    functionsFileUpload --= functionsFileUploadToRemove.map(_._1)
    functionsFileUploadToRemove.foreach({
      case (_, f) => f.fsc.functionsFileUploadGenerated -= f.id
    })
    session.fsSystem.stats.event(StatEvent.GC_FILE_UPLOAD, n = functionsFileUploadToRemove.size)
    // logger.info(s"Removed ${functionsFileUploadToRemove.size} functions")

    val currentFunctionsFileDownload = functionsFileDownload.toVector
    val functionsFileDownloadToRemove = currentFunctionsFileDownload.filter(_._2.keepAliveAt < keepAliveOlderThan)
    functionsFileDownload --= functionsFileDownloadToRemove.map(_._1)
    functionsFileDownloadToRemove.foreach({
      case (_, f) => f.fsc.functionsFileDownloadGenerated -= f.id
    })
    session.fsSystem.stats.event(StatEvent.GC_FILE_DOWNLOAD, n = functionsFileDownloadToRemove.size)
    // logger.info(s"Removed ${functionsFileDownloadToRemove.size} functions")
  }
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

  val logger = LoggerFactory.getLogger(getClass.getName)

  var idSeq = 0L

  private implicit def __fsContextOpt: Option[FSContext] = None

  private implicit def __fsPageOpt: Option[FSPage] = None

  private implicit def __fsSessionOpt: Option[FSSession] = Some(this)

  private implicit def __fsSystem: FSSystem = fsSystem

  def nextID(prefix: String = ""): String = {
    session.synchronized {
      session.idSeq += 1
      prefix + session.idSeq.toString
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
    session.fsSystem.checkSpace()
    val copy = new Request.Wrapper(req)
    val page = new FSPage(nextID(), this, copy, onPageUnload = onPageUnload, debugLbl = debugLbl)
    if (logger.isTraceEnabled) logger.trace(s"Created page ${page.id}")
    fsSystem.stats.event(StatEvent.CREATE_PAGE)
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

  def gc(keepAliveOlderThan: Long): Unit = {
    val currentPages = pages.toVector
    val pagesToRemove = currentPages.filter(_._2.keepAliveAt < keepAliveOlderThan)
    pages --= pagesToRemove.map(_._1)
    fsSystem.stats.event(StatEvent.GC_PAGE, n = pagesToRemove.size)
    // logger.info(s"Removed ${pagesToRemove.size} pages")

    val currentAnonymousPages = anonymousPages.toVector
    val anonymousPagesToRemove = currentAnonymousPages.filter(_._2.keepAliveAt < keepAliveOlderThan)
    anonymousPages --= anonymousPagesToRemove.map(_._1)
    fsSystem.stats.event(StatEvent.GC_ANON_PAGE, n = anonymousPagesToRemove.size)
    // logger.info(s"Removed ${pagesToRemove.size} anonymous pages")

    pages.values.foreach(_.gc(keepAliveOlderThan))
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
        checkSpace()
        val id = IdGen.secureId()
        val session = new FSSession(id, this)
        implicit val __fsContextOpt: Option[FSContext] = None
        implicit val __fsPageOpt: Option[FSPage] = None
        implicit val __fsSessionOpt: Option[FSSession] = Some(session)
        stats.event(StatEvent.CREATE_SESSION)
        this.synchronized(sessions.put(session.id, session))
        FSSession.logger.info(s"Created session: session_id=${session.id}, evt_type=create_session")
        Option((List(HttpCookie.build(FSSessionIdCookieName, session.id).path("/").build()), inSession(session)))
      case None => None
    }
  }

  def handleCallbackException(ex: Throwable): Js = {
    ex.printStackTrace()
    if (__fsSystem.debug) {
      Js.alert(s"Internal error: ${ex.getMessage} (showing because in debug mode)")
    } else {
      Js.alert(s"Internal error")
    }
  }

  def handleFileUploadCallbackException(ex: Throwable): Js = {
    ex.printStackTrace()
    if (__fsSystem.debug) {
      Js.alert(s"Internal error: ${ex.getMessage} (showing because in debug mode)")
    } else {
      Js.alert(s"Internal error")
    }
  }

  def handleFileDownloadCallbackException(ex: Throwable): Response = {
    ex.printStackTrace()

    if (__fsSystem.debug) {
      ServerError.InternalServerError(s"Internal error: ${ex.getMessage} (showing because in debug mode)")
    } else {
      ServerError.InternalServerError
    }
  }

  override def handlerNoSession(response: JettyServerResponse, callback: Callback)(implicit req: Request): Option[Response] = {
    val cookies = Option(Request.getCookies(req)).getOrElse(Collections.emptyList).asScala
    val sessionIdOpt = cookies.find(_.getName == FSSessionIdCookieName).map(_.getValue)
    val sessionOpt = sessionIdOpt.flatMap(sessionId => sessions.get(sessionId))

    import RoutingHandlerHelper._

    Some(req).collect {
      case Post(FSPrefix, "cb", pageId, funcId) =>
        // Callback invocation:

        implicit val __fsSessionOpt: Option[FSSession] = None
        implicit val __fsPageOpt: Option[FSPage] = None
        implicit val __fsFuncOpt: Option[FSFunc] = None
        implicit val __fsContextOpt: Option[FSContext] = None
        sessionOpt.map(implicit session => {
          implicit val __fsSessionOpt: Option[FSSession] = Option(session)
          session.pages.get(pageId).map(implicit page => {
            implicit val __fsPageOpt: Option[FSPage] = Option(page)
            page.functions.get(funcId).map(implicit fsFunc => {
              implicit val __fsFuncOpt = Option(fsFunc)
              implicit val __fsContextOpt: Option[FSContext] = __fsFuncOpt.map(_.fsc)
              fsFunc.keepAlive()
              val start = System.currentTimeMillis()
              Content.Source.asStringAsync(req, Request.getCharset(req)).whenComplete((arg, failure) => {
                val rslt: Js = Option(failure) match {
                  case Some(failure) =>
                    handleCallbackException(failure)
                  case None =>
                    try {
                      if (logger.isTraceEnabled) logger.trace(s"Running callback on thread ${Thread.currentThread()}...")
                      val start = System.currentTimeMillis()
                      val rslt = fsFunc.func(arg)
                      if (logger.isTraceEnabled) logger.trace(s"Finished in ${System.currentTimeMillis() - start}ms (thread ${Thread.currentThread()}) with result JS with ${rslt.cmd.length} chars")
                      rslt
                    } catch {
                      case trowable =>
                        if (logger.isTraceEnabled) logger.trace(s"Finished with EXCEPTION in ${System.currentTimeMillis() - start}ms (thread ${Thread.currentThread()}): $trowable")
                        handleCallbackException(trowable)
                    }
                }
                if (logger.isTraceEnabled) logger.trace(s"Invoke func: session_id=${session.id}, page_id=$pageId, func_id=$funcId, took_ms=${System.currentTimeMillis() - start}, response_size_bytes=${rslt.cmd.getBytes.size}, evt_type=invk_func")
                stats.event(StatEvent.USE_CALLBACK, additionalFields = Seq("func_name" -> fsFunc.fullPath))
                Ok.js(rslt).respond(response, callback)
              })
              VoidResponse
            }).getOrElse({
              stats.event(StatEvent.NOT_FOUND_CALLBACK)
              onFuncNotFoundForAjaxReq(funcId)(page, req)
            })
          }).getOrElse({
            stats.event(StatEvent.NOT_FOUND_PAGE)
            onPageNotFoundForAjaxReq(pageId, funcId)(session, req)
          })
        }).getOrElse({
          stats.event(StatEvent.NOT_FOUND_SESSION)
          onSessionNotFoundForAjaxReq(sessionIdOpt)
        })

      case Post(FSPrefix, "file-upload", pageId, funcId) =>
        implicit val __fsSessionOpt: Option[FSSession] = None
        implicit val __fsPageOpt: Option[FSPage] = None
        implicit val __fsFuncOpt: Option[FSFunc] = None
        implicit val __fsContextOpt: Option[FSContext] = None

        sessionOpt.map(implicit session => {
          implicit val __fsSessionOpt: Option[FSSession] = Option(session)
          session.pages.get(pageId) match {
            case Some(page) =>
              implicit val __fsPageOpt: Option[FSPage] = Option(page)
              page.functionsFileUpload.get(funcId) match {
                case Some(fSFileUpload) =>

                  fSFileUpload.keepAlive()

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
                          fSFileUpload.func(parts.asScala.map(part => try {
                            new FSUploadedFile(
                              name = part.getName,
                              submittedFileName = part.getFileName,
                              contentType = part.getHeaders.get(HttpHeader.CONTENT_TYPE),
                              content = Content.Source.asByteBuffer(part.getContentSource).array
                            )
                          } finally {
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
                  } else
                    Ok.js(Js.alert("Not multipart form data"))

                case None =>
                  stats.event(StatEvent.NOT_FOUND_FILE_UPLOAD)
                  onFuncNotFoundForAjaxReq(funcId)(page, req)
              }
            case None =>
              stats.event(StatEvent.NOT_FOUND_PAGE)
              onPageNotFoundForAjaxReq(pageId, funcId)(session, req)
          }
        }).getOrElse({
          stats.event(StatEvent.NOT_FOUND_SESSION)
          onSessionNotFoundForAjaxReq(sessionIdOpt)
        })

      // File downloads:
      case Get(FSPrefix, "file-download", pageId, funcId, _) =>
        sessionOpt.map(implicit session => {
          session.pages.get(pageId) match {
            case Some(page) =>
              page.functionsFileDownload.get(funcId) match {
                case Some(fSFileDownload) =>
                  fSFileDownload.keepAlive()
                  try {
                    val bytes = fSFileDownload.func()
                    Ok.binaryWithContentType(bytes, fSFileDownload.contentType)
                  } catch {
                    case ex: Exception => handleFileDownloadCallbackException(ex)
                  }
                case None =>
                  stats.event(StatEvent.NOT_FOUND_FILE_DOWNLOAD)
                  onFuncNotFoundForStdReq(pageId, funcId)(session, req)
              }
            case None =>
              stats.event(StatEvent.NOT_FOUND_PAGE)
              onPageNotFoundForStdReq(pageId)(session, req)
          }
        }).getOrElse({
          stats.event(StatEvent.NOT_FOUND_SESSION)
          onSessionNotFoundForStdReq(sessionIdOpt)
        })

      // Anonymous pages:
      case Get(FSPrefix, "anon", anonymousPageId, _) =>
        sessionOpt.map(implicit session => {
          session.anonymousPages.get(anonymousPageId).map(anonymousPage => {
            anonymousPage.keepAlive()
            val nodeSeq = session.createPage(implicit fsc => anonymousPage.renderAsString(), onPageUnload = () => {
              anonymousPage.onPageUnload()
            }, debugLbl = Some(s"page for anon page ${anonymousPage.debugLbl.getOrElse(s"with id ${anonymousPage.id}")}"))
            Ok.html(nodeSeq)
          }).getOrElse({
            stats.event(StatEvent.NOT_FOUND_ANON_PAGE)
            onAnonymousPageNotFoundForStdReq(anonymousPageId)
          })
        }).getOrElse({
          stats.event(StatEvent.NOT_FOUND_SESSION)
          onSessionNotFoundForStdReq(sessionIdOpt)
        })
    }
  }

  def onSessionNotFoundForAjaxReq(sessionId: Option[String])(implicit req: Request): Response = {
    logger.warn(s"Session not found: session_id=$sessionId, evt_type=session_id_not_found")
    val js: Js = if (Option(Request.getParameters(req).getValue("ignore_errors")).map(_ == "true").getOrElse(false)) Js.void
    else Js.confirm(s"Session $sessionId not found, will reload", Js.reload())
    Ok.js(js)
  }

  def onSessionNotFoundForWebsocketReq(sessionId: String)(implicit session: Session): Js = {
    logger.warn(s"Session not found: session_id=$sessionId, evt_type=session_id_not_found")
    val js: Js = if (session.getUpgradeRequest.getParameterMap.asScala.get("ignore_errors").flatMap(_.asScala.headOption).map(_ == "true").getOrElse(false)) Js.void
    else Js.confirm(s"Session $sessionId not found, will reload", Js.reload())
    js
  }

  def onSessionNotFoundForStdReq(sessionId: Option[String])(implicit req: Request): Response = {
    logger.warn(s"Session not found: session_id=$sessionId, evt_type=session_not_found")
    Redirect.temporaryRedirect("/")
  }

  def onPageNotFoundForAjaxReq(pageId: String, funcId: String)(implicit session: FSSession, req: Request): Response = {
    logger.warn(s"Page not found: session_id=${session.id}, page_id=$pageId, func_id=$funcId, evt_type=page_id_not_found")
    val js: Js = if (Option(Request.getParameters(req).getValue("ignore_errors")).map(_ == "true").getOrElse(false)) Js.void
    else Js.confirm(s"Page $pageId not found", Js.redirectTo("/"))
    Ok.js(js)
  }

  def onPageNotFoundForWebsocketReq(sessionId: String, pageId: String)(implicit session: Session): Js = {
    logger.warn(s"Page not found: session_id=$sessionId, page_id=$pageId, evt_type=page_id_not_found")
    val js: Js = if (session.getUpgradeRequest.getParameterMap.asScala.get("ignore_errors").flatMap(_.asScala.headOption).map(_ == "true").getOrElse(false)) Js.void
    else Js.confirm(s"Page $pageId not found, will reload", Js.reload())
    js
  }

  def onPageNotFoundForStdReq(pageId: String)(implicit session: FSSession, req: Request): Response = {
    logger.warn(s"Page not found: session_id=${session.id}, page_id=$pageId, evt_type=page_id_not_found")
    Redirect.temporaryRedirect("/")
  }

  def onFuncNotFoundForAjaxReq(funcId: String)(implicit page: FSPage, req: Request): Response = {
    val ignoreErrors = Option(Request.getParameters(req).getValue("ignore_errors")).map(_ == "true").getOrElse(false)
    logger.warn(s"Function not found: session_id=${page.session.id}, page_id=${page.id}, func_id=$funcId, evt_type=func_id_not_found, ignore_errors=$ignoreErrors")
    val js: Js = if (ignoreErrors) Js.void
    else Js.confirm(s"Func $funcId not found, will reload", Js.reload())
    Ok.js(js)
  }

  def onFuncNotFoundForStdReq(pageId: String, funcId: String)(implicit session: FSSession, req: Request): Response = {
    logger.warn(s"Function not found: session_id=${session.id}, page_id=${pageId}, func_id=$funcId, evt_type=func_id_not_found")
    Redirect.temporaryRedirect("/")
  }

  def onAnonymousPageNotFoundForStdReq(anonymousPageId: String)(implicit session: FSSession): Response = {
    logger.warn(s"Anonymous page not found: session_id=${session.id}, anonymous_page_id=$anonymousPageId, evt_type=anonymous_page_id_not_found")
    Redirect.temporaryRedirect("/")
  }

  def onPageUnload()(implicit fsc: FSContext): Js = {
    //    fsc.session.synchronized {
    //      FSSession.logger.trace(s"REMOVE PAGE lived ${((System.currentTimeMillis() - fsc.page.createdAt) / 1000d).formatted("%.2f")}s session_id=${fsc.session.id} page_id=${fsc.page.id} #functions=${fsc.page.functions.keys.size} #functions_file_upload=${fsc.page.functionsFileUpload.keys.size} #functions_file_download=${fsc.page.functionsFileDownload.keys.size}, evt_type=remove_page")
    //      fsc.session.pages.remove(fsc.page.id)
    //    }
    Js.void
  }

  def allKeepAlivesIterable: Iterable[Long] = sessions.values.flatMap(_.allKeepAlivesIterable)

  def checkSpace(): Unit = synchronized {
    if (
      Runtime.getRuntime.totalMemory() > 1000 * 1024 * 1024 &&
        Runtime.getRuntime.freeMemory() < 50 * 1024 * 1024
    ) {
      if (logger.isTraceEnabled) logger.trace("Less than 50MB available")
      if (logger.isTraceEnabled) logger.trace(s"#Sessions: ${sessions.size} #Pages: ${sessions.map(_._2.pages.size).sum} #Funcs: ${sessions.map(_._2.pages.map(_._2.functions.size).sum).sum}")
      val allKeepAlives = allKeepAlivesIterable.toVector.sorted
      if (logger.isTraceEnabled) logger.trace(s"Found ${allKeepAlives.size} keep alives")
      val deleteOlderThan: Long = allKeepAlives.drop(allKeepAlives.size / 2).headOption.getOrElse(0L)
      if (logger.isTraceEnabled) logger.trace(s"Removing everything with keepalive older than ${System.currentTimeMillis() - deleteOlderThan}ms")
      gc(deleteOlderThan)
      val start = System.currentTimeMillis()
      System.gc()
      if (logger.isTraceEnabled) logger.trace(s"Run GC in ${System.currentTimeMillis() - start}ms")
      checkSpace()
    }
  }

  def gc(keepAliveOlderThan: Long): Unit = {
    val current = sessions.toVector
    val toRemove = current.filter(_._2.keepAliveAt < keepAliveOlderThan)
    sessions --= toRemove.map(_._1)
    stats.event(StatEvent.GC_SESSION, n = toRemove.size)
    if (logger.isTraceEnabled) logger.trace(s"Removed ${toRemove.size} sessions: ${toRemove.map(_._1).mkString(", ")}")
    sessions.values.foreach(_.gc(keepAliveOlderThan))
  }
}
