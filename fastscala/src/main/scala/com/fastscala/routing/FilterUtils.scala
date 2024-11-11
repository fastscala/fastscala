package com.fastscala.routing

import com.fastscala.routing.resp.Response
import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.server.Request

object FilterUtils {

  def onlyHandleHtmlRequests(handle: => Option[Response])(implicit req: Request): Option[Response] =
    if (Option(req.getHeaders.get(HttpHeader.ACCEPT)).getOrElse("").contains("text/html")) handle else None

  def ignoreNonHtmlRequests(handle: => Option[Response])(implicit req: Request): Option[Response] = {
    if (Option(req.getHeaders.get(HttpHeader.ACCEPT)).exists(accept => !accept.contains("text/html") && !accept.contains("text/*") && !accept.contains("*/*"))) None else handle
  }
}
