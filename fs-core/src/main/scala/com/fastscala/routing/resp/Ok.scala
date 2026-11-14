package com.fastscala.routing.resp

import com.fastscala.js.Js
import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.server.Response as JettyServerResponse
import org.eclipse.jetty.util.Callback

import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.file.{Files, Path}

object Ok {
  def html(ns: String) = new OkTextBased("<!DOCTYPE html>" + ns, "text/html;charset=utf-8")

  def plain(text: String) = new OkTextBased(text, "text/plain;charset=utf-8")

  def css(css: String) = new OkTextBased(css, "text/css;charset=utf-8")

  def js(js: Js) = new Response {
    val ContentType: String = "text/javascript;charset=utf-8"

    override def status: HttpStatus = HttpStatuses.OK

    override def respond(response: JettyServerResponse, callback: Callback): Boolean = {
      super.respond(response, callback)
      val responseHeaders = response.getHeaders
      responseHeaders.put(HttpHeader.CONTENT_TYPE, ContentType)
      // Known length: lets Jetty skip chunked transfer encoding.
      responseHeaders.put(HttpHeader.CONTENT_LENGTH, js.lengthUtf8Enc)
      js.writeUtf8Enc(response, callback)
      response.write(true, ByteBuffer.allocate(0), callback)
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
