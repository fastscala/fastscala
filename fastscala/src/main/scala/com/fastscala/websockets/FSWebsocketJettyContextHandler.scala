package com.fastscala.websockets

import com.fastscala.core.FSSystem

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.ContextHandler
import org.eclipse.jetty.websocket.api.util.WebSocketConstants
import org.eclipse.jetty.websocket.server.WebSocketUpgradeHandler

import java.time.Duration

object  FSWebsocketJettyContextHandler {

  def apply(server: Server, context: String)(implicit fss: FSSystem): ContextHandler = {
    val contextHandler = new ContextHandler(context)

    contextHandler.setHandler(WebSocketUpgradeHandler.from(server, contextHandler, container => {
      container.setMaxTextMessageSize(65536)
      container.setIdleTimeout(Duration.ofSeconds(600))
      container.addMapping("/ws", (rq, rs, cb) => {
        if (rq.getSubProtocols.contains("fs")) {
          // according to spec,
          // response in its handshake must set header[Sec-WebSocket-Protocol] to the subprotocol that the server has selected.
          rs.getHeaders.put(WebSocketConstants.SEC_WEBSOCKET_PROTOCOL, "fs")
          new FSJettyWebsocketEndpoint
        } else
          null
      })
    }))
    contextHandler
  }
}
