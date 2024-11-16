package com.fastscala.scala_xml.server

import com.fastscala.scala_xml.utils.FSOptimizedResourceHandler
import com.fastscala.server.JettyServerHelper
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.handler.ContextHandler

trait JettyServerHelperWithFSOptimizedResourceHandler extends JettyServerHelper {

  val jettyStaticFilesHandler = new FSOptimizedResourceHandler(resourceRoots)

  override def prependToHandlerList: List[Handler] =
    new ContextHandler(jettyStaticFilesHandler, "/static/optimized") ::
      super.prependToHandlerList

}
