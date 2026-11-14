package com.fastscala.routing.resp

import com.fastscala.js.Js
import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.server.Response as JettyServerResponse
import org.eclipse.jetty.util.{BufferUtil, Callback}

import java.io.OutputStream
import java.nio.file.{Files, Path}

object Ok {
  def html(ns: String) = new OkTextBased("<!DOCTYPE html>" + ns, "text/html;charset=utf-8")

  def plain(text: String) = new OkTextBased(text, "text/plain;charset=utf-8")

  def css(css: String) = new OkTextBased(css, "text/css;charset=utf-8")

  private val JsContentType: String = "text/javascript;charset=utf-8"

  /** JavaScript, with its Content-Length (computed first: see [[jsStreaming]] to skip that). */
  def js(js: Js) = new Response {
    override def status: HttpStatus = HttpStatuses.OK

    override def respond(response: JettyServerResponse, callback: Callback): Boolean = {
      super.respond(response, callback)
      val responseHeaders = response.getHeaders
      responseHeaders.put(HttpHeader.CONTENT_TYPE, JsContentType)
      // Known length: lets Jetty skip chunked transfer encoding.
      // responseHeaders.put(HttpHeader.CONTENT_LENGTH, js.lengthUtf8Enc)
      writeJs(js, response, callback)
      true
    }
  }

  /**
   * JavaScript, written as it's produced, without a Content-Length (so Jetty sends it chunked). Computing the length
   * ([[Js.lengthUtf8Enc]], as [[js]] does) can cost as much as writing it: e.g. a Js with HTML in it (a rerender's)
   * serializes and escapes the HTML once just to count its bytes, and then again to write it; here, only once.
   */
  def jsStreaming(js: Js) = new Response {
    override def status: HttpStatus = HttpStatuses.OK

    override def respond(response: JettyServerResponse, callback: Callback): Boolean = {
      super.respond(response, callback)
      response.getHeaders.put(HttpHeader.CONTENT_TYPE, JsContentType)
      writeJs(js, response, callback)
      true
    }
  }

  /**
   * Writes `js`, then ends the content (an empty last write), once it's all written; completes `callback` once, after
   * that (or fails it, on the first failure).
   */
  private def writeJs(js: Js, response: JettyServerResponse, callback: Callback): Unit =
    js.writeUtf8Enc(response, Callback.from(
      () => response.write(true, BufferUtil.EMPTY_BUFFER, callback),
      (t: Throwable) => callback.failed(t)
    ))

  def utf8WithContentType(text: String, contentType: String, attachmentFileName: Option[String] = None) = {
    val _contentType = contentType
    new BinaryResponse {
      override def contents: Array[Byte] = text.getBytes("UTF-8")

      override def attachmentFileNameOpt: Option[String] = attachmentFileName

      override def contentType: String = _contentType

      override def status: HttpStatus = HttpStatuses.OK
    }
  }

  def utf8AutoDetectContentType(text: String, fileName: String, attachmentFileName: Option[String] = None) = new BinaryResponse {
    override def contents: Array[Byte] = text.getBytes("UTF-8")

    override def attachmentFileNameOpt: Option[String] = attachmentFileName

    override def contentType: String = Files.probeContentType(Path.of(fileName))

    override def status: HttpStatus = HttpStatuses.OK
  }

  def binaryAutoDetectContentType(bytes: Array[Byte], fileName: String, attachmentFileName: Option[String] = None) = new BinaryResponse {
    override def contents: Array[Byte] = bytes

    override def attachmentFileNameOpt: Option[String] = attachmentFileName

    override def contentType: String = Files.probeContentType(Path.of(fileName))

    override def status: HttpStatus = HttpStatuses.OK
  }

  def outputStreamAutoDetectContentType(streamOutput: OutputStream => Unit, fileName: String, size: Option[Int], attachmentFileName: Option[String] = None) = {
    val _size = size
    new OutputStreamResponse {
      override def stream(os: OutputStream): Unit = streamOutput(os)

      override def size: Option[Int] = _size

      override def attachmentFileNameOpt: Option[String] = attachmentFileName

      override def contentType: String = Files.probeContentType(Path.of(fileName))

      override def status: HttpStatus = HttpStatuses.OK
    }
  }

  def outputStreamWithContentType(streamOutput: OutputStream => Unit, contentType: String, fileName: String, size: Option[Int], attachmentFileName: Option[String] = None) = {
    val _contentType = contentType
    val _size = size
    new OutputStreamResponse {
      override def stream(os: OutputStream): Unit = streamOutput(os)

      override def attachmentFileNameOpt: Option[String] = attachmentFileName

      override def size: Option[Int] = _size

      override def contentType: String = _contentType

      override def status: HttpStatus = HttpStatuses.OK
    }
  }

  def binaryWithContentType(bytes: Array[Byte], contentType: String, fileName: String, attachmentFileName: Option[String] = None) = {
    val _contentType = contentType
    new BinaryResponse {
      override def contents: Array[Byte] = bytes

      override def attachmentFileNameOpt: Option[String] = attachmentFileName

      override def contentType: String = _contentType

      override def status: HttpStatus = HttpStatuses.OK
    }
  }
}
