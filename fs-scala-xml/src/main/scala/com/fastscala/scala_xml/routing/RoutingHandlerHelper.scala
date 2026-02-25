package com.fastscala.scala_xml.routing

import com.fastscala.core.{FSContext, FSSession, FSSystem, NoFSPageImplProvided}
import com.fastscala.routing.RoutingHandlerHelper
import com.fastscala.routing.resp.{Ok, Response}
import com.fastscala.scala_xml.routing.resp.ResponseScalaXmlSupport.*
import com.fastscala.scala_xml.utils.{FSPageImplWithFSContext, RenderableWithFSContext}
import org.eclipse.jetty.server.{Handler, Request, Response as JettyServerResponse}
import org.eclipse.jetty.util.Callback

import scala.xml.NodeSeq

abstract class ScalaXmlRoutingHandlerHelper(implicit fss: FSSystem) extends RoutingHandlerHelper {

  def servePage(renderable: RenderableWithFSContext, debugLbl: Option[String] = None)(implicit req: Request, session: FSSession): Response = {
    servePageWithImpl(new FSPageImplWithFSContext {
      override def toString: String = "Page served with servePage"

      override def render()(implicit fsc: FSContext): NodeSeq = renderable.render()(using fsc)
    }, debugLbl)
  }

  def servePageWithImpl(page: FSPageImplWithFSContext, debugLbl: Option[String] = None)(implicit req: Request, session: FSSession): Response = {
    val rendered: NodeSeq = session.createAndRenderPageWithImpl(page, debugLbl = debugLbl.orElse(Some(req.getHttpURI.getPath)))
    Ok.html(rendered).addHeaders(
      "Cache-Control" -> "no-cache, max-age=0, no-store"
      , "Pragma" -> "no-cache"
      , "Expires" -> "-1"
    )
  }
}
