package com.fastscala.core

import com.fastscala.js.Js
import com.fastscala.routing.resp.Response
import com.fastscala.stats.FSStats
import com.fastscala.utils.Missing
import com.typesafe.config.Config
import org.eclipse.jetty.http.HttpCookie
import org.eclipse.jetty.server.{Handler, Request}
import org.eclipse.jetty.websocket.api.Session

/**
 * A FastScala system: the sessions (with their pages, callbacks, ...), its configuration and stats, and the hooks an
 * app can override (what callbacks do, what happens when something isn't found, ...). It's also the Jetty [[Handler]]
 * that serves FastScala's own requests (callbacks, file uploads/downloads, anonymous pages, keep-alives).
 *
 * See [[FSSystemImpl]], the implementation (extend it to override the hooks).
 */
trait FSSystem extends Handler {

  // ---------- identity, configuration and stats ----------

  def appName: String

  def stats: FSStats

  def config: Config

  def useRandomIds: Boolean

  def debug: Boolean

  /** When a callback isn't found because its context was deleted, say where that context was. */
  def debugDeletedContextsFullPath: Boolean

  // Which events are recorded in the stats:

  def eventsEnableCreateSession: Boolean

  def eventsEnableCreatePage: Boolean

  def eventsEnableCreateAnonPage: Boolean

  def eventsEnableCreateFileDownload: Boolean

  def eventsEnableCreateFileUpload: Boolean

  def eventsEnableCreateCallback: Boolean

  def eventsEnableUseCallback: Boolean

  def eventsEnableGcSession: Boolean

  def eventsEnableGcPage: Boolean

  def eventsEnableGcAnonPage: Boolean

  def eventsEnableGcFileDownload: Boolean

  def eventsEnableGcCallback: Boolean

  def eventsEnableGcFileUpload: Boolean

  def eventsEnableNotFoundSession: Boolean

  def eventsEnableNotFoundPage: Boolean

  def eventsEnableNotFoundAnonPage: Boolean

  def eventsEnableNotFoundFileDownload: Boolean

  def eventsEnableNotFoundFileUpload: Boolean

  def eventsEnableNotFoundCallback: Boolean

  // ---------- sessions ----------

  /** The live sessions, by id. */
  def sessions: collection.mutable.Map[String, FSSession]

  /** The name of the cookie with the session id. */
  def FSSessionIdCookieName: String

  /** The prefix of FastScala's own paths (e.g. `/fs/...`). */
  def FSPrefix: String

  def createSession(): FSSession

  /** A new session, and the Js that sets its cookie. */
  def createSessionAndSetSessionId(): (FSSession, Js)

  /**
   * Runs `inSession` in the request's session (a new one, if it has none, except for websocket requests): with the
   * cookies to set (the new session's), if any.
   */
  def inSession[T](inSession: FSSession => T)(implicit req: Request): Option[(List[HttpCookie], T)]

  /** Frees memory if needed (when less is free than configured): deletes the least interesting pages and sessions. */
  def gc(): Unit

  /** Frees memory now: deletes the configured percentage of the least interesting pages (and sessions left empty). */
  def freeUpSpace(): Unit

  // ---------- hooks: executing ----------

  def executeKeepAlive()(implicit page: FSPage): Js

  def executeCallback(arg: String)(implicit func: FSFunc): Js

  def executeFileUpload(files: Seq[FSUploadedFile])(implicit fileUpload: FSFileUpload): Js

  /** The response with a callback's (or keep-alive's, file upload's) Js. */
  def respondWithJs(js: Js): Response

  /** The response with a rendered anonymous page. */
  def respondWithAnonymousPage(rendered: AnyRef): Response

  /** Js run on the client before each callback. */
  def callbackClientSideBefore(implicit fsc: FSContext): Js

  /** Js run on the client after each callback. */
  def callbackClientSideAfter(implicit fsc: FSContext): Js

  /** Js run on the client when a callback fails. */
  def callbackClientSideOnError(implicit fsc: FSContext): Js

  /** Js run on the client when a callback times out. */
  def callbackClientSideOnTimeout(implicit fsc: FSContext): Js

  // ---------- hooks: errors ----------

  /** The Js sent back when a callback throws. */
  def handleCallbackException(ex: Throwable): Js

  /** The Js sent back when a file upload's callback throws. */
  def handleFileUploadCallbackException(ex: Throwable): Js

  /** The response when a file download's callback throws. */
  def handleFileDownloadCallbackException(ex: Throwable): Response

  // ---------- hooks: not found (an expired page, session, ...) ----------

  def onKeepAliveNotFound(missing: Missing.Value, sessionId: Option[String], pageId: String, session: Option[FSSession], page: Option[FSPage])(implicit req: Request): Js

  def onCallbackNotFound(missing: Missing.Value, sessionId: Option[String], pageId: String, functionId: String, session: Option[FSSession], page: Option[FSPage])(implicit req: Request): Js

  def onFileUploadNotFound(missing: Missing.Value, sessionId: Option[String], pageId: String, functionId: String, session: Option[FSSession], page: Option[FSPage])(implicit req: Request): Js

  def onFileDownloadNotFound(missing: Missing.Value, sessionId: Option[String], pageId: String, functionId: String, session: Option[FSSession], page: Option[FSPage])(implicit req: Request): Response

  def onAnonymousPageNotFound(missing: Missing.Value, sessionId: Option[String], anonPageId: String, session: Option[FSSession], page: Option[FSPage])(implicit req: Request): Response

  def onWebsocketNotFound(missing: Missing.Value, sessionId: String, pageId: String, session: Option[FSSession], page: Option[FSPage])(implicit wsSession: Session): Js
}
