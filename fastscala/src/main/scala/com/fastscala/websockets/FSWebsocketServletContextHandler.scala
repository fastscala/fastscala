package com.fastscala.websockets

import com.fastscala.core.FSSystem
import jakarta.websocket.server.ServerEndpointConfig
import org.eclipse.jetty.ee10.servlet.ServletContextHandler
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer

import java.util.Collections

class FSWebsocketServletContextHandler(implicit fss: FSSystem)
  extends ServletContextHandler("/" + fss.FSPrefix) {

  JakartaWebSocketServletContainerInitializer.configure(this, (servletContext, serverContainer) => {
    serverContainer.setDefaultMaxTextMessageBufferSize(65535)
    serverContainer.setDefaultMaxSessionIdleTimeout(600000)
    serverContainer.addEndpoint(
      ServerEndpointConfig.Builder
        .create(classOf[FSJakartaWebsocketEndpoint], "/ws")
        .configurator(new ServerEndpointConfig.Configurator {
          override def getEndpointInstance[T](endpointClass: Class[T]): T =
            new FSJakartaWebsocketEndpoint().asInstanceOf[T]
        })
        .subprotocols(Collections.singletonList("fs"))
        .build()
    )
  })
}
