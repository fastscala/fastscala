package com.fastscala.routing.resp

import org.apache.commons.io.IOUtils
import com.fastscala.js.Js
import org.eclipse.jetty.http.{HttpCookie, HttpHeader, MimeTypes}
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.Response as JettyServerResponse
import org.eclipse.jetty.util.{BufferUtil, Callback}

import java.nio.file.{Files, Path}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, OutputStream}


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

object VoidResponse extends Response {
  val status = HttpStatuses.NoContent

  override def respond(response: JettyServerResponse, callback: Callback): Boolean = true
}

trait TextResponse extends Response {
  def contents: String

  def contentType: String

  override def respond(response: JettyServerResponse, callback: Callback): Boolean = {
    super.respond(response, callback)
    response.getHeaders.put(HttpHeader.CONTENT_TYPE, contentType)
    val charsetName = Option(MimeTypes.getCharsetFromContentType(contentType)).getOrElse("UTF-8")
    response.write(true, BufferUtil.toBuffer(contents.getBytes(charsetName)), callback)
    true
  }
}

trait BinaryResponse extends Response {
  def contents: Array[Byte]

  def contentType: String

  override final def respond(response: JettyServerResponse, callback: Callback): Boolean = {
    super.respond(response, callback)
    val responseHeaders = response.getHeaders
    responseHeaders.put(HttpHeader.CONTENT_TYPE, contentType)
    responseHeaders.put(HttpHeader.CONTENT_LENGTH, contents.length)
    response.write(true, BufferUtil.toBuffer(contents), callback)
    true
  }
}

trait OutputStreamResponse extends Response {
  def stream(os: OutputStream): Unit

  def contentType: String

  override final def respond(response: JettyServerResponse, callback: Callback): Boolean = {
    super.respond(response, callback)
    val responseHeaders = response.getHeaders
    responseHeaders.put(HttpHeader.CONTENT_TYPE, contentType)
    val baos = new ByteArrayOutputStream()
    val os = Content.Sink.asOutputStream(response)
    stream(baos)
    IOUtils.copy(new ByteArrayInputStream(baos.toByteArray), os)
    os.flush()
    os.close()
    true
  }
}

trait RedirectResponse extends TextResponse {

  def redirectTo: String

  def contentType: String = "text/css;charset=utf-8"

  override def contents: String = {
    s"""<html>
       |<head><title>Moved</title></head>
       |<body>=Moved=<p>This page has moved to <a href="$redirectTo">$redirectTo</a>.</p></body>
       |</html>""".stripMargin
  }

  override final def respond(response: JettyServerResponse, callback: Callback): Boolean = {
    response.getHeaders.put(HttpHeader.LOCATION, redirectTo)
    super.respond(response, callback)
  }
}

object Ok {
  def html(ns: String) = new OkHtml(ns)

  def plain(text: String) = new OkTextBased(text, "text/plain;charset=utf-8")

  def css(css: String) = new OkTextBased(css, "text/css;charset=utf-8")

  def js(js: Js) = new OkTextBased(js.cmd, "text/javascript;charset=utf-8")

  def utf8WithContentType(text: String, contentType: String) = {
    val _contentType = contentType
    new BinaryResponse {
      override def contents: Array[Byte] = text.getBytes("UTF-8")

      override def contentType: String = _contentType

      override def status: HttpStatus = HttpStatuses.OK
    }
  }

  def utf8AutoDetectContentType(text: String, fileName: String) = new BinaryResponse {
    override def contents: Array[Byte] = text.getBytes("UTF-8")

    override def contentType: String = Files.probeContentType(Path.of(fileName))

    override def status: HttpStatus = HttpStatuses.OK
  }

  def binaryAutoDetectContentType(bytes: Array[Byte], fileName: String) = new BinaryResponse {
    override def contents: Array[Byte] = bytes

    override def contentType: String = Files.probeContentType(Path.of(fileName))

    override def status: HttpStatus = HttpStatuses.OK
  }

  def outputStreamAutoDetectContentType(streamOutput: OutputStream => Unit, fileName: String) = new OutputStreamResponse {
    override def stream(os: OutputStream): Unit = streamOutput(os)

    override def contentType: String = Files.probeContentType(Path.of(fileName))

    override def status: HttpStatus = HttpStatuses.OK
  }

  def outputStreamWithContentType(streamOutput: OutputStream => Unit, contentType: String) = {
    val _contentType = contentType
    new OutputStreamResponse {
      override def stream(os: OutputStream): Unit = streamOutput(os)

      override def contentType: String = _contentType

      override def status: HttpStatus = HttpStatuses.OK
    }
  }

  def binaryWithContentType(bytes: Array[Byte], contentType: String) = {
    val _contentType = contentType
    new BinaryResponse {
      override def contents: Array[Byte] = bytes

      override def contentType: String = _contentType

      override def status: HttpStatus = HttpStatuses.OK
    }
  }
}

object Redirect {

  def apply(redirectTo: String, status: HttpStatus): RedirectResponse = {
    val _redirectTo = redirectTo
    val _status: HttpStatus = status
    new RedirectResponse {
      override def redirectTo: String = _redirectTo

      override def status: HttpStatus = _status
    }
  }

  def multipleChoices(redirectTo: String) = Redirect(redirectTo, HttpStatuses.MultipleChoices)

  def movedPermanently(redirectTo: String) = Redirect(redirectTo, HttpStatuses.MovedPermanently)

  def found(redirectTo: String) = Redirect(redirectTo, HttpStatuses.Found)

  def seeOther(redirectTo: String) = Redirect(redirectTo, HttpStatuses.SeeOther)

  def notModified(redirectTo: String) = Redirect(redirectTo, HttpStatuses.NotModified)

  def useProxy(redirectTo: String) = Redirect(redirectTo, HttpStatuses.UseProxy)

  def temporaryRedirect(redirectTo: String) = Redirect(redirectTo, HttpStatuses.TemporaryRedirect)

  def permanentRedirect(redirectTo: String) = Redirect(redirectTo, HttpStatuses.PermanentRedirect)

}

object ClientError {
  def apply(httpStatus: HttpStatus, cause: String = null) = new Response {
    override def status: HttpStatus = httpStatus

    override final def respond(response: JettyServerResponse, callback: Callback): Boolean = {
      super.respond(response, callback)
      JettyServerResponse.writeError(response.getRequest(), response, callback, status.code, cause)
      true
    }
  }

  def BadRequest(cause: String) = apply(HttpStatuses.BadRequest, cause)

  val BadRequest = apply(HttpStatuses.BadRequest)

  val Unauthorized = apply(HttpStatuses.Unauthorized)

  val PaymentRequired = apply(HttpStatuses.PaymentRequired)

  val Forbidden = apply(HttpStatuses.Forbidden)

  val NotFound = apply(HttpStatuses.NotFound)

  val MethodNotAllowed = apply(HttpStatuses.MethodNotAllowed)

  val NotAcceptable = apply(HttpStatuses.NotAcceptable)

  val ProxyAuthenticationRequired = apply(HttpStatuses.ProxyAuthenticationRequired)

  val RequestTimeout = apply(HttpStatuses.RequestTimeout)

  val Conflict = apply(HttpStatuses.Conflict)

  val Gone = apply(HttpStatuses.Gone)

  val LengthRequired = apply(HttpStatuses.LengthRequired)

  val PreconditionFailed = apply(HttpStatuses.PreconditionFailed)

  val PayloadTooLarge = apply(HttpStatuses.PayloadTooLarge)

  val UriTooLong = apply(HttpStatuses.UriTooLong)

  val UnsupportedMediaType = apply(HttpStatuses.UnsupportedMediaType)

  val RangeNotSatisfiable = apply(HttpStatuses.RangeNotSatisfiable)

  val ExpectationFailed = apply(HttpStatuses.ExpectationFailed)

  val ImATeapot = apply(HttpStatuses.ImATeapot)

  val EnhanceYourCalm = apply(HttpStatuses.EnhanceYourCalm)

  val MisdirectedRequest = apply(HttpStatuses.MisdirectedRequest)

  val UnprocessableEntity = apply(HttpStatuses.UnprocessableEntity)

  val Locked = apply(HttpStatuses.Locked)

  val FailedDependency = apply(HttpStatuses.FailedDependency)

  val TooEarly = apply(HttpStatuses.TooEarly)

  val UpgradeRequired = apply(HttpStatuses.UpgradeRequired)

  val PreconditionRequired = apply(HttpStatuses.PreconditionRequired)

  val TooManyRequests = apply(HttpStatuses.TooManyRequests)

  val RequestHeaderFieldsTooLarge = apply(HttpStatuses.RequestHeaderFieldsTooLarge)

  val RetryWith = apply(HttpStatuses.RetryWith)

  val BlockedByParentalControls = apply(HttpStatuses.BlockedByParentalControls)

  val UnavailableForLegalReasons = apply(HttpStatuses.UnavailableForLegalReasons)
}

object ServerError {
  def apply(httpStatus: HttpStatus, cause: String = null) = new Response {
    override def status: HttpStatus = httpStatus

    override final def respond(response: JettyServerResponse, callback: Callback): Boolean = {
      super.respond(response, callback)
      JettyServerResponse.writeError(response.getRequest(), response, callback, status.code, cause)
      true
    }
  }

  def InternalServerError(cause: String) = apply(HttpStatuses.InternalServerError, cause)

  val InternalServerError = apply(HttpStatuses.InternalServerError)

  val NotImplemented = apply(HttpStatuses.NotImplemented)

  val BadGateway = apply(HttpStatuses.BadGateway)

  val ServiceUnavailable = apply(HttpStatuses.ServiceUnavailable)

  val GatewayTimeout = apply(HttpStatuses.GatewayTimeout)

  val HttpVersionNotSupported = apply(HttpStatuses.HttpVersionNotSupported)

  val VariantAlsoNegotiates = apply(HttpStatuses.VariantAlsoNegotiates)

  val InsufficientStorage = apply(HttpStatuses.InsufficientStorage)

  val LoopDetected = apply(HttpStatuses.LoopDetected)

  val BandwidthLimitExceeded = apply(HttpStatuses.BandwidthLimitExceeded)

  val NotExtended = apply(HttpStatuses.NotExtended)

  val NetworkAuthenticationRequired = apply(HttpStatuses.NetworkAuthenticationRequired)

  val NetworkReadTimeout = apply(HttpStatuses.NetworkReadTimeout)

  val NetworkConnectTimeout = apply(HttpStatuses.NetworkConnectTimeout)
}

class OkHtml(ns: String) extends TextResponse {
  override def status: HttpStatus = HttpStatuses.OK

  override def contentType: String = "text/html;charset=utf-8"

  def docType: String = "<!DOCTYPE html>"

  override def contents: String = docType + "\n" + ns
}

class OkTextBased(text: String, val contentType: String) extends TextResponse {
  override def status: HttpStatus = HttpStatuses.OK

  override def contents: String = text
}
