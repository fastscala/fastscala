package com.fastscala.websockets

import com.fastscala.code.FSSystem
import jakarta.websocket.{Endpoint, EndpointConfig, Session}

import scala.util.Try

@jakarta.websocket.server.ServerEndpoint("/ws")
class FSJakartaWebsocketEndpoint()(implicit fss: FSSystem) extends Endpoint {

  override def onOpen(session: Session, config: EndpointConfig): Unit = {
    implicit val _session = session
    (for {
      sessionIdValues <- Option(session.getRequestParameterMap.get("sessionId"))
      sessionId <- Try(sessionIdValues.get(0)).toOption
      pageIdValues <- Option(session.getRequestParameterMap.get("pageId"))
      pageId <- Try(pageIdValues.get(0)).toOption
    } {
      fss.sessions.get(sessionId).map(fsSession => {
        fsSession.pages.get(pageId).map(page => {
          page.wsLock.synchronized {
            page.wsSession = Some(session)
            if (page.wsQueue.nonEmpty) {
              session.getBasicRemote.sendText(page.wsQueue.reverse.reduce(_ & _).cmd)
              page.wsQueue = Nil
            }
          }
        }).getOrElse({
          session.getBasicRemote.sendText(fss.onPageNotFoundForWebsocketReq(sessionId, pageId).cmd)
        })
      }).getOrElse({
        session.getBasicRemote.sendText(fss.onSessionNotFoundForWebsocketReq(sessionId).cmd)
      })
    })
  }
}
