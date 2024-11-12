package com.fastscala.routing

import com.fastscala.core.{FSSession, FSSystem, FSXmlEnv, FSXmlSupport}
import com.fastscala.routing.resp.{Ok, Response}
import com.fastscala.utils.RenderableWithFSContext
import org.eclipse.jetty.server.{Handler, Request, Response as JettyServerResponse}
import org.eclipse.jetty.util.Callback

abstract class RoutingHandlerNoSessionHelper extends Handler.Abstract {

  def handlerNoSession(response: JettyServerResponse, callback: Callback)(implicit req: Request): Option[Response]

  override def handle(request: Request, response: JettyServerResponse, callback: Callback): Boolean = {
    handlerNoSession(response, callback)(request).map(resp => {
      resp.respond(response, callback)
    }).getOrElse(false)
  }
}

abstract class RoutingHandlerHelper(implicit fss: FSSystem) extends RoutingHandlerNoSessionHelper {

  def servePage[E <: FSXmlEnv](using env: FSXmlSupport[E])(renderable: RenderableWithFSContext[E], debugLbl: Option[String] = None)(implicit req: Request, session: FSSession): Response = {
    session.createPage(implicit fsc => Ok.html(renderable.render())
      .addHeaders(
        "Cache-Control" -> "no-cache, max-age=0, no-store"
        , "Pragma" -> "no-cache"
        , "Expires" -> "-1"
      ), debugLbl = debugLbl.orElse(Some(req.getHttpURI.getPath))
    )
  }

  def handlerInSession(response: JettyServerResponse, callback: Callback)(implicit req: Request, session: FSSession): Option[Response]


  override def handle(request: Request, response: JettyServerResponse, callback: Callback): Boolean = {
    handlerNoSession(response, callback)(request) match {
      case Some(resp) =>
        resp.respond(response, callback)
      case None =>
        fss.inSession {
          implicit session => handlerInSession(response, callback)(request, session)
        }(request).flatMap({
          case (cookies, resp) =>
            cookies.foreach(JettyServerResponse.addCookie(response, _))
            resp.map(resp => {
              resp.respond(response, callback)
            })
        }).getOrElse(false)
    }
  }
}
