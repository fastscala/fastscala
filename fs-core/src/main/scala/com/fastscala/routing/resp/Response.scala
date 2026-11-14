package com.fastscala.routing.resp

import org.eclipse.jetty.http.HttpCookie
import org.eclipse.jetty.server.Response as JettyServerResponse
import org.eclipse.jetty.util.Callback

trait Response {

  def status: HttpStatus

  val headers = collection.mutable.ListBuffer[(String, String)]()

  val cookiesToAdd = collection.mutable.ListBuffer[HttpCookie]()

  def addHeader(name: String, value: String): this.type = {
    headers += ((name, value))
    this
  }

  def addCookie(cookie: HttpCookie): this.type = {
    cookiesToAdd += cookie
    this
  }

  def addHeaders(headers: (String, String)*): this.type = {
    headers.foreach({
      case (name, value) => addHeader(name, value)
    })
    this
  }

  def respond(response: JettyServerResponse, callback: Callback): Boolean = {
    response.setStatus(status.code)
    val responseHeaders = response.getHeaders
    headers.foreach {
      case (name, value) => responseHeaders.put(name, value)
    }
    cookiesToAdd.foreach(JettyServerResponse.addCookie(response, _))
    false
  }
}
