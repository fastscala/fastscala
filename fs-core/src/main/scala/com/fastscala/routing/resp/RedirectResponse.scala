package com.fastscala.routing.resp

import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.server.Response as JettyServerResponse
import org.eclipse.jetty.util.Callback

import java.nio.ByteBuffer
import java.nio.charset.Charset

trait RedirectResponse extends TextResponse {

  def redirectTo: String

  def contentType: String = "text/html;charset=utf-8"

  override protected def encodedContents(charset: Charset): List[ByteBuffer] = List(ByteBuffer.wrap(
    s"""<html>
       |<head><title>Moved</title></head>
       |<body>=Moved=<p>This page has moved to <a href="$redirectTo">$redirectTo</a>.</p></body>
       |</html>""".stripMargin.getBytes(charset)
  ))

  override final def respond(response: JettyServerResponse, callback: Callback): Boolean = {
    response.getHeaders.put(HttpHeader.LOCATION, redirectTo)
    super.respond(response, callback)
  }
}
