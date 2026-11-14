package com.fastscala.scala_xml.routing.resp

import com.fastscala.routing.resp.{HttpStatus, HttpStatuses, Response}
import com.fastscala.scala_xml.utils.XmlStream2
import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.server.Response as JettyServerResponse
import org.eclipse.jetty.util.Callback

import scala.xml.NodeSeq

/**
 * A 200 HTML response, written as it's serialized: the doctype and then `ns`, written by [[XmlStream2]] in UTF-8
 * straight into the response, in chunks. The page is never turned into one String or byte array, so there's no
 * Content-Length upfront: Jetty sends it with chunked transfer encoding (HTTP/1.1), or just ends the stream (HTTP/2).
 * (The last chunk ends the content, so a page that fits in one chunk is sent in a single write.)
 *
 * The writes block the handling thread until Jetty has taken each chunk. If writing fails midway (e.g. the client
 * went away), the response is already committed: the callback is failed, so Jetty aborts it.
 */
class OkHtmlStreaming(ns: NodeSeq) extends Response {

  override def status: HttpStatus = HttpStatuses.OK

  /** UTF-8 only: XmlStream2 writes UTF-8. */
  final def contentType: String = "text/html;charset=utf-8"

  def docType: String = "<!DOCTYPE html>"

  override def respond(response: JettyServerResponse, callback: Callback): Boolean = {
    super.respond(response, callback)
    response.getHeaders.put(HttpHeader.CONTENT_TYPE, contentType)
    // No Content-Length: the length isn't known until everything is written.
    try {
      XmlStream2.writeSeq(ns, response, prefix = docType + "\n") // the last chunk ends the content
      callback.succeeded()
    } catch {
      case t: Throwable => callback.failed(t)
    }
    true
  }
}
