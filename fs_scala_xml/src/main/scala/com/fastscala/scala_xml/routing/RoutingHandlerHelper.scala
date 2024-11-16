package com.fastscala.scala_xml.routing

import com.fastscala.core.{FSSession, FSSystem}
import com.fastscala.routing.RoutingHandlerHelper
import com.fastscala.scala_xml.routing.resp.ResponseScalaXmlSupport.*
import com.fastscala.routing.resp.{Ok, Response}
import com.fastscala.scala_xml.utils.RenderableWithFSContext
import org.eclipse.jetty.server.{Handler, Request, Response as JettyServerResponse}
import org.eclipse.jetty.util.Callback

abstract class ScalaXmlRoutingHandlerHelper(implicit fss: FSSystem) extends RoutingHandlerHelper {

  def servePage(renderable: RenderableWithFSContext, debugLbl: Option[String] = None)(implicit req: Request, session: FSSession): Response = {
    session.createPage(implicit fsc => Ok.html(renderable.render())
      .addHeaders(
        "Cache-Control" -> "no-cache, max-age=0, no-store"
        , "Pragma" -> "no-cache"
        , "Expires" -> "-1"
      ), debugLbl = debugLbl.orElse(Some(req.getHttpURI.getPath))
    )
  }
}
