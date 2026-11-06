package com.fastscala.routing.resp

import org.apache.commons.io.IOUtils
import com.fastscala.js.Js
import org.eclipse.jetty.http.{HttpCookie, HttpHeader, MimeTypes}
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.Response as JettyServerResponse
import org.eclipse.jetty.util.{BufferUtil, Callback}

import java.nio.ByteBuffer
import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.{Files, Path}
import java.util.concurrent.ConcurrentHashMap
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

object Ok {
  def html(ns: String) = new OkHtml(ns)

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

  def docType: String = OkHtml.DefaultDocType

  // Writes the doctype and the page as two buffers, instead of copying the whole page into `docType + "\n" + ns` first.
  override protected def encodedContents(charset: Charset): List[ByteBuffer] = {
    val dt = docType
    val prefix =
      if (dt == OkHtml.DefaultDocType && charset == StandardCharsets.UTF_8) OkHtml.DefaultPrefixUtf8.asReadOnlyBuffer() // shared constant: own position per response
      else ByteBuffer.wrap((dt + "\n").getBytes(charset))
    List(prefix, ByteBuffer.wrap(ns.getBytes(charset)))
  }
}

object OkHtml {
  val DefaultDocType = "<!DOCTYPE html>"

  private val DefaultPrefixUtf8: ByteBuffer = ByteBuffer.wrap((DefaultDocType + "\n").getBytes(StandardCharsets.UTF_8)).asReadOnlyBuffer()
}

class OkTextBased(text: String, val contentType: String) extends TextResponse {
  override def status: HttpStatus = HttpStatuses.OK

  override protected def encodedContents(charset: Charset): List[ByteBuffer] = List(ByteBuffer.wrap(text.getBytes(charset)))
}
