package com.fastscala.routing.resp

import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.server.Response as JettyServerResponse
import org.eclipse.jetty.util.{BufferUtil, Callback}

trait BinaryResponse extends Response {
  def contents: Array[Byte]

  def contentType: String

  def attachmentFileNameOpt: Option[String]

  override final def respond(response: JettyServerResponse, callback: Callback): Boolean = {
    super.respond(response, callback)
    val responseHeaders = response.getHeaders
    responseHeaders.put(HttpHeader.CONTENT_TYPE, contentType)
    attachmentFileNameOpt.foreach(fileName => responseHeaders.put(HttpHeader.CONTENT_DISPOSITION, s"attachment; filename=\"$fileName\""))
    responseHeaders.put(HttpHeader.CONTENT_LENGTH, contents.length.toString)
    response.write(true, BufferUtil.toBuffer(contents), callback)
    true
  }
}
