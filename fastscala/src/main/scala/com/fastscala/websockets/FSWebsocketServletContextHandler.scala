package com.fastscala.websockets

import com.fastscala.code.FSSystem
import jakarta.servlet.ServletContext
import jakarta.websocket.server.{ServerContainer, ServerEndpointConfig}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer

import java.util.Collections

class FSWebsocketServletContextHandler(server: Server)(implicit fss: FSSystem)
  extends ServletContextHandler(server, "/" + fss.FSPrefix) {

  JakartaWebSocketServletContainerInitializer.configure(this, new JakartaWebSocketServletContainerInitializer.Configurator {
    override def accept(servletContext: ServletContext, serverContainer: ServerContainer): Unit = {
      serverContainer.setDefaultMaxTextMessageBufferSize(65535)
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
    }
  })

}
