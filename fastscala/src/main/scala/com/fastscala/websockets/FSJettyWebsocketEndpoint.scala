package com.fastscala.websockets

import com.fastscala.core.FSSystem
import org.eclipse.jetty.websocket.api.{Session, Callback}
import org.eclipse.jetty.websocket.api.annotations._

import scala.util.Try
import org.slf4j.LoggerFactory

object FSJettyWebsocketEndpoint {
  val logger = LoggerFactory.getLogger(getClass.getName)
}

@WebSocket(autoDemand = true)
class FSJettyWebsocketEndpoint(implicit fss: FSSystem) {

  @OnWebSocketOpen
  def onOpen(session: Session): Unit = {
    implicit val _session = session
    val params = session.getUpgradeRequest().getParameterMap()
    for {
      sessionIdValues <- Option(params.get("sessionId"))
      sessionId <- Try(sessionIdValues.get(0)).toOption
      pageIdValues <- Option(params.get("pageId"))
      pageId <- Try(pageIdValues.get(0)).toOption
    } {
      fss.sessions.get(sessionId).map(fsSession => {
        fsSession.pages.get(pageId).map(page => {
          page.wsLock.synchronized {
            page.wsSession = Some(session)
            FSJettyWebsocketEndpoint.logger.info(s"Websocket session[sessionId=$sessionId, pageId=$pageId] opened")
            if (page.wsQueue.nonEmpty) {
              sendText(page.wsQueue.reverse.reduce(_ & _).cmd)
              page.wsQueue = Nil
            }
          }
        }).getOrElse({
          sendText(fss.onPageNotFoundForWebsocketReq(sessionId, pageId).cmd)
        })
      }).getOrElse({
        sendText(fss.onSessionNotFoundForWebsocketReq(sessionId).cmd)
      })
    }
  }

  @OnWebSocketError
  def onError(t: Throwable): Unit = {
    FSJettyWebsocketEndpoint.logger.info("Websocket session on error", t)
  }


  @OnWebSocketClose
  def onClose(statusCode: Int, reason: String) = {
    FSJettyWebsocketEndpoint.logger.info(s"Websocket session closed by $reason($statusCode)")
  }

  def sendText(text: String)(implicit session: Session): Unit = {
    // block until session complete sendText
    Callback.Completable.`with`(cb => session.sendText(text, cb)).get()
  }
}
