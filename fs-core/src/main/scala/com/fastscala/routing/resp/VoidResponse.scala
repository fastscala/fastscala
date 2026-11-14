package com.fastscala.routing.resp

import org.eclipse.jetty.server.Response as JettyServerResponse
import org.eclipse.jetty.util.Callback

object VoidResponse extends Response {
  val status = HttpStatuses.NoContent

  override def respond(response: JettyServerResponse, callback: Callback): Boolean = true
}
