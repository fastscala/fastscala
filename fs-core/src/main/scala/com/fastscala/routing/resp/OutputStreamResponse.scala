package com.fastscala.routing.resp

import org.apache.commons.io.IOUtils
import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.Response as JettyServerResponse
import org.eclipse.jetty.util.Callback

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, OutputStream}

trait OutputStreamResponse extends Response {
  def stream(os: OutputStream): Unit

  def contentType: String

  def size: Option[Int]

  def attachmentFileNameOpt: Option[String]

  override final def respond(response: JettyServerResponse, callback: Callback): Boolean = {
    super.respond(response, callback)
    val responseHeaders = response.getHeaders
    responseHeaders.put(HttpHeader.CONTENT_TYPE, contentType)
    attachmentFileNameOpt.foreach(fileName => responseHeaders.put(HttpHeader.CONTENT_DISPOSITION, s"attachment; filename=\"$fileName\""))
    size.foreach(size => responseHeaders.put(HttpHeader.CONTENT_LENGTH, size.toString))
    val baos = new ByteArrayOutputStream()
    val os = Content.Sink.asOutputStream(response)
    stream(baos)
    IOUtils.copy(new ByteArrayInputStream(baos.toByteArray), os)
    os.flush()
    os.close()
    true
  }
}
