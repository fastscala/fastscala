package com.fastscala.routing.resp

import com.fastscala.js.{Js, JsWriter}
import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.server.Response as JettyServerResponse
import org.eclipse.jetty.util.Callback

import java.io.OutputStream
import java.nio.file.{Files, Path}

object Ok {
  def html(ns: String) = new OkTextBased("<!DOCTYPE html>" + ns, "text/html;charset=utf-8")

  def plain(text: String) = new OkTextBased(text, "text/plain;charset=utf-8")

  def css(css: String) = new OkTextBased(css, "text/css;charset=utf-8")

  private val JsContentType: String = "text/javascript;charset=utf-8"

  /**
   * JavaScript, written as it's produced, by `writer` (e.g. fs-scala-xml's `XmlJsStream`), which ends the content. No
   * Content-Length is computed (it'd cost as much as writing it: e.g. a rerender's, with HTML in it): Jetty sets one only
   * if the whole JavaScript is written at once, with the last write (a short one, with XmlJsStream); otherwise it's sent
   * chunked.
   */
  def js(js: Js, writer: JsWriter = JsWriter.Recursive) = new Response {
    override def status: HttpStatus = HttpStatuses.OK

    override def respond(response: JettyServerResponse, callback: Callback): Boolean = {
      super.respond(response, callback)
      response.getHeaders.put(HttpHeader.CONTENT_TYPE, JsContentType)
      writer.writeJs(js, response, callback, endContent = true)
      true
    }
  }


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
