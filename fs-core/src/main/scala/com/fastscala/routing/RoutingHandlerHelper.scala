package com.fastscala.routing

import com.fastscala.core.{FSSession, FSSystem}
import com.fastscala.routing.resp.{Ok, Response}
import org.eclipse.jetty.server.{Handler, Request, Response as JettyServerResponse}
import org.eclipse.jetty.util.Callback

abstract class RoutingHandlerNoSessionHelper extends Handler.Abstract {

  def handlerNoSession(response: JettyServerResponse, callback: Callback)(implicit req: Request): Option[Response]

  override def handle(request: Request, response: JettyServerResponse, callback: Callback): Boolean = {
    handlerNoSession(response, callback)(using request).map(resp => {
      resp.respond(response, callback)
    }).getOrElse(false)
  }
}

abstract class RoutingHandlerHelper(implicit fss: FSSystem) extends RoutingHandlerNoSessionHelper {

  def handlerInSession(response: JettyServerResponse, callback: Callback)(implicit req: Request, session: FSSession): Option[Response]


  override def handle(request: Request, response: JettyServerResponse, callback: Callback): Boolean = {
    handlerNoSession(response, callback)(using request) match {
      case Some(resp) =>
        resp.respond(response, callback)
      case None =>
        fss.inSession {
          implicit session => handlerInSession(response, callback)(using request, session)
        }(using request).flatMap({
          case (cookies, resp) =>
            cookies.foreach(JettyServerResponse.addCookie(response, _))
            resp.map(resp => {
              resp.respond(response, callback)
            })
        }).getOrElse(false)
    }
  }
}
