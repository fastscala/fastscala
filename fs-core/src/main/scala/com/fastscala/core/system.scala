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


class FSSystem(val appName: String = "app", val stats: FSStats = new FSStats())
  extends RoutingHandlerNoSessionHelper {

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

  def createSession(): FSSession = {
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
    session
  }

  def createSessionAndSetSessionId(): (FSSession, Js) = {
    val session = createSession()
    (session, JS.setCookie(FSSessionIdCookieName, session.id, path = Some("/")))
  }

  def inSession[T](inSession: FSSession => T)(implicit req: Request): Option[(List[HttpCookie], T)] = {
    val cookies = Option(Request.getCookies(req)).getOrElse(Collections.emptyList).asScala
    cookies.filter(_.getName == FSSessionIdCookieName).map(_.getValue).flatMap(sessions.get(_)).headOption match {
      case Some(session) => Option((Nil, inSession(session)))
      case None if !req.getHttpURI.getPath.startsWith("/" + FSPrefix + "/ws") =>
        val session = createSession()
        Option((List(HttpCookie.build(FSSessionIdCookieName, session.id).path("/").build()), inSession(session)))
      case None => None
    }
  }

  def handleCallbackException(ex: Throwable): Js = {
    ex.printStackTrace()
    stats.callbackErrorsTotal.inc()
    if (__fsSystem.debug) {
      JS.alert(s"Internal error: $ex (showing because in debug mode)")
    } else {
      JS.alert(s"Internal error")
    }
  }

  def handleFileUploadCallbackException(ex: Throwable): Js = {
    ex.printStackTrace()
    stats.fileUploadCallbackErrorsTotal.inc()
    if (__fsSystem.debug) {
      JS.alert(s"Internal error: $ex (showing because in debug mode)")
    } else {
      JS.alert(s"Internal error")
    }
  }

  def handleFileDownloadCallbackException(ex: Throwable): Response = {
    ex.printStackTrace()
    stats.fileDownloadCallbackErrorsTotal.inc()
    if (__fsSystem.debug) {
      ServerError.InternalServerError(s"Internal error: $ex (showing because in debug mode)")
    } else {
      ServerError.InternalServerError
    }
  }

  override def handlerNoSession(response: JettyServerResponse, callback: Callback)(implicit req: Request): Option[Response] = {
    val cookies = Option(Request.getCookies(req)).getOrElse(Collections.emptyList).asScala

    val sessionIdOpt = cookies.find(_.getName == FSSessionIdCookieName).map(_.getValue).orElse(Option(Request.getParameters(req).getValue(FSSessionIdCookieName)).filter(_ != ""))
    val sessionOpt = sessionIdOpt.flatMap(sessionId => sessions.get(sessionId))

    Some(req).collect {
      // Keep alive:
      case Post(FSPrefix, "ka", pageId) =>
        sessionOpt.map(implicit session => {
          session.pages.get(pageId).map(implicit page => {
            stats.keepAliveInvocationsTotal.inc()
            page.autoKeepAliveAt = System.currentTimeMillis()
            Ok.js(JS.void)
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
                      val rslt = executeCallback(arg)
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
                if (logger.isTraceEnabled)
                  logger.trace(
                    s"Invoke func: session_id=${session.id}, page_id=$pageId, func_id=$funcId, took_ms=${System.currentTimeMillis() - start}, response_size_bytes=${rslt.cmd.getBytes.size}, evt_type=invk_func"
                  )
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
                      parts.asScala.map(part =>
                        try {
                          executeFileUpload(
                            new FSUploadedFile(
                              name = part.getName,
                              submittedFileName = part.getFileName,
                              contentType = part.getHeaders.get(HttpHeader.CONTENT_TYPE),
                              bytes = () => Content.Source.asByteBuffer(part.getContentSource).array,
                              inputStream = () => Content.Source.asInputStream(part.getContentSource)
                            ) :: Nil
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
                        }
                      ).reduceOption(_ & _).getOrElse(Js.Void)
                  }
                  Ok.js(rslt).respond(response, callback)
                })
                VoidResponse
              } else {
                Ok.js(JS.alert("Not multipart form data"))
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
                fSFileDownload.content match {
                  case Left(array) =>
                    val bytes = array()
                    Ok.binaryWithContentType(bytes, fSFileDownload.contentType)
                  case Right(outputStream) =>
                    Ok.outputStreamWithContentType(outputStream, fSFileDownload.contentType)
                }
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
            val nodeSeq = session.createPage(
              implicit fsc => anonymousPage.renderAsString(),
              onPageUnload = () => {
                anonymousPage.onPageUnload()
              },
              debugLbl = Some(s"page for anon page ${anonymousPage.debugLbl.getOrElse(s"with id ${anonymousPage.id}")}")
            )
            Ok.html(nodeSeq)
          }).getOrElse(onAnonymousPageNotFound(Missing.AnonPage, sessionId = sessionIdOpt, anonPageId = anonymousPageId, session = Some(session), page = None))
        }).getOrElse(onAnonymousPageNotFound(Missing.Session, sessionId = sessionIdOpt, anonPageId = anonymousPageId, session = sessionOpt, page = None))
    }
  }
  def executeKeepAlive()(implicit page: FSPage): Js = Js.Void

  def executeCallback(arg: String)(implicit func: FSFunc): Js = func.func(arg)
  
  def executeFileUpload(files: Seq[FSUploadedFile])(implicit fileUpload: FSFileUpload): Js = fileUpload.func(files)
  
  def onKeepAliveNotFound(missing: Missing.Value, sessionId: Option[String], pageId: String, session: Option[FSSession], page: Option[FSPage])(implicit req: Request): Js = {
    missing.updateStats()
    JS.void
  }

  def onCallbackNotFound(missing: Missing.Value, sessionId: Option[String], pageId: String, functionId: String, session: Option[FSSession], page: Option[FSPage])(implicit req: Request): Js = {
    missing.updateStats()
    if (Option(Request.getParameters(req).getValue("ignore_errors")).map(_ == "true").getOrElse(false)) JS.void
    else {
      (for {
        page <- page
        if debug
        context <- page.deletedCallbacksContextPath.get(functionId)
        mesg = s"Callback $functionId not found - it's context was DELETED: $context"
      } yield {
        logger.warn(mesg)
        JS.confirm(mesg + ", confirm reload", JS.reload())
      }).getOrElse(JS.confirm(s"Page has expired, please reload", JS.reload()))
    }
  }

  def onFileUploadNotFound(missing: Missing.Value, sessionId: Option[String], pageId: String, functionId: String, session: Option[FSSession], page: Option[FSPage])(implicit req: Request): Js = {
    missing.updateStats()
    if (Option(Request.getParameters(req).getValue("ignore_errors")).map(_ == "true").getOrElse(false)) JS.void
    else JS.confirm(s"Page has expired, please reload", JS.reload())
  }

  def onFileDownloadNotFound(
                              missing: Missing.Value,
                              sessionId: Option[String],
                              pageId: String,
                              functionId: String,
                              session: Option[FSSession],
                              page: Option[FSPage]
                            )(implicit req: Request): Response = {
    missing.updateStats()
    Redirect.temporaryRedirect("/")
  }

  def onAnonymousPageNotFound(missing: Missing.Value, sessionId: Option[String], anonPageId: String, session: Option[FSSession], page: Option[FSPage])(implicit req: Request): Response = {
    missing.updateStats()
    Redirect.temporaryRedirect("/")
  }

  def onWebsocketNotFound(missing: Missing.Value, sessionId: String, pageId: String, session: Option[FSSession], page: Option[FSPage])(implicit wsSession: Session): Js = {
    missing.updateStats()
    if (wsSession.getUpgradeRequest.getParameterMap.asScala.get("ignore_errors").flatMap(_.asScala.headOption).map(_ == "true").getOrElse(false)) JS.void
    else JS.confirm(s"Page has expired, please reload", JS.reload())
  }

  def callbackClientSideBefore(implicit fsc: FSContext): Js = Js.Void

  def callbackClientSideAfter(implicit fsc: FSContext): Js = Js.Void

  def callbackClientSideOnError(implicit fsc: FSContext): Js = Js.Void

  def callbackClientSideOnTimeout(implicit fsc: FSContext): Js = Js.Void

  def gc(): Unit = synchronized {
    val minFreeSpacePercent = config.getDouble("com.fastscala.core.gc.run-when-less-than-mem-free-percent")
    if ((Runtime.getRuntime.freeMemory().toDouble / Runtime.getRuntime.totalMemory() * 100) < minFreeSpacePercent) {
      System.gc()
      if ((Runtime.getRuntime.freeMemory().toDouble / Runtime.getRuntime.totalMemory() * 100) < minFreeSpacePercent) {
        if (logger.isTraceEnabled) logger.trace(s"Less than ${minFreeSpacePercent.formatted("%.2f%%")} space available, freeing up space...")
        freeUpSpace()
        gc()
      }
    }
  }

  def freeUpSpace(): Unit = synchronized {
    stats.gcRunsTotal.inc()
    val start = System.currentTimeMillis()

    val percentOfPagesToDelete: Double = config.getDouble("com.fastscala.core.gc.percent-of-pages-to-delete")

    val pagesSortedByInterest = sessions.flatMap(_._2.pages.values).toSeq.sortBy(p => (p.isAlive_?, p.keepAliveAt))
    val pagesToDelete: Set[FSPage] = pagesSortedByInterest.take(math.max(1, (percentOfPagesToDelete / 100.0) * pagesSortedByInterest.size).toInt).toSet

    // Delete sessions with no more pages:
    val sessionsToDelete = sessions.values.filter(s => s.pages.isEmpty || s.pages.values.forall(p => pagesToDelete.contains(p))).toSet
    sessionsToDelete.foreach(_.delete())

    // Delete pages:
    pagesToDelete.groupBy(_.session).foreach({
      case (session, toDelete) if !sessionsToDelete.contains(session) => session.deletePages(toDelete)
      case _ =>
    })

    stats.gcTimeTotal.inc(io.prometheus.metrics.model.snapshots.Unit.millisToSeconds(System.currentTimeMillis() - start))
  }
}
