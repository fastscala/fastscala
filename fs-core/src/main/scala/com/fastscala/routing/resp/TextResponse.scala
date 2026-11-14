package com.fastscala.routing.resp

import org.eclipse.jetty.http.{HttpHeader, MimeTypes}
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.Response as JettyServerResponse
import org.eclipse.jetty.util.{BufferUtil, Callback}

import java.nio.ByteBuffer
import java.nio.charset.{Charset, StandardCharsets}
import java.util.concurrent.ConcurrentHashMap

trait TextResponse extends Response {

  def contentType: String

  /**
   * The body encoded with `charset` (taken from [[contentType]], UTF-8 by default), as one or more buffers written in order.
   * Several buffers avoid concatenating the body into a single String first (see [[OkHtml]]).
   */
  protected def encodedContents(charset: Charset): List[ByteBuffer]

  override def respond(response: JettyServerResponse, callback: Callback): Boolean = {
    super.respond(response, callback)
    val ct = contentType
    val buffers = encodedContents(TextResponse.charsetFor(ct))
    val responseHeaders = response.getHeaders
    responseHeaders.put(HttpHeader.CONTENT_TYPE, ct)
    // Known length: lets Jetty skip chunked transfer encoding.
    responseHeaders.put(HttpHeader.CONTENT_LENGTH, buffers.foldLeft(0L)(_ + _.remaining()))
    TextResponse.writeAll(response, buffers, callback)
    true
  }
}

object TextResponse {

  // Content types are almost always a handful of constants: cache the (string-parsing) charset lookup per content type.
  private val charsetCache = new ConcurrentHashMap[String, Charset]()

  /** Writes the buffers in order, each write starting only after the previous one succeeded; the last one ends the content. */
  def writeAll(sink: Content.Sink, buffers: List[ByteBuffer], callback: Callback): Unit = buffers match {
    case Nil => sink.write(true, BufferUtil.EMPTY_BUFFER, callback)
    case last :: Nil => sink.write(true, last, callback)
    case head :: tail => sink.write(false, head, Callback.from(() => writeAll(sink, tail, callback), (t: Throwable) => callback.failed(t)))
  }

  def charsetFor(contentType: String): Charset =
    if (contentType == null) StandardCharsets.UTF_8
    else charsetCache.computeIfAbsent(contentType, ct =>
      Option(MimeTypes.getCharsetFromContentType(ct)).map(Charset.forName).getOrElse(StandardCharsets.UTF_8)
    )
}
