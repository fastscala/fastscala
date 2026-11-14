package com.fastscala.scala_xml.routing.resp

import com.fastscala.routing.resp.{HttpStatus, HttpStatuses, Response, TextResponse}
import com.fastscala.scala_xml.utils.XmlStream
import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.Response as JettyServerResponse
import org.eclipse.jetty.util.Callback

import java.io.OutputStreamWriter
import scala.xml.NodeSeq

/**
 * A 200 HTML response, like [[OkHtml]], but written as it's serialized: the doctype and then `ns`, written by
 * [[XmlStream]] straight into the response's output stream (through an encoding Writer), in chunks. The page is never
 * turned into one String or byte array, so there's no Content-Length upfront: Jetty sends it with chunked transfer
 * encoding (HTTP/1.1), or just ends the stream (HTTP/2).
 *
 * The writes block the handling thread until Jetty has taken each chunk (as with any OutputStream response). If
 * writing fails midway (e.g. the client went away), the response is already committed: the callback is failed, so
 * Jetty aborts it.
 */
class OkHtmlStreaming(ns: NodeSeq) extends Response {

  override def status: HttpStatus = HttpStatuses.OK

  def contentType: String = "text/html;charset=utf-8"

  def docType: String = "<!DOCTYPE html>"

  override def respond(response: JettyServerResponse, callback: Callback): Boolean = {
    super.respond(response, callback)
    val ct = contentType
    response.getHeaders.put(HttpHeader.CONTENT_TYPE, ct)
    // No Content-Length: the length isn't known until everything is written.
    try {
      val writer = new OutputStreamWriter(Content.Sink.asOutputStream(response), TextResponse.charsetFor(ct))
      writer.write(docType)
      writer.write('\n')
      XmlStream.writeSeq(ns, writer)
      writer.close() // flushes the encoder, and closes the stream: the last write, ending the content
      callback.succeeded()
    } catch {
      case t: Throwable => callback.failed(t)
    }
    true
  }
}
