package com.fastscala.server

import com.fastscala.core.{FSSession, FSSystem, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.utils.RenderableWithFSContext
import org.eclipse.jetty.http.{HttpHeader, HttpCookie, MimeTypes}
import org.eclipse.jetty.server.{Request, Response => JettyServerResponse, Handler}
import org.eclipse.jetty.util.{BufferUtil, Callback}

import java.nio.file.{Path, Files}
import java.nio.charset.StandardCharsets.UTF_8

case class HttpStatus(name: String, code: Int)

object HttpStatuses {

  val Continue = HttpStatus("Continue", 100)
  val SwitchingProtocols = HttpStatus("SwitchingProtocols", 101)
  val Processing = HttpStatus("Processing", 102)

  val EarlyHints = HttpStatus("EarlyHints", 103)

  val OK = HttpStatus("OK", 200)
  val Created = HttpStatus("Created", 201)
  val Accepted = HttpStatus("Accepted", 202)
  val NonAuthoritativeInformation = HttpStatus("NonAuthoritativeInformation", 203)
  val NoContent = HttpStatus("NoContent", 204)
  val ResetContent = HttpStatus("ResetContent", 205)
  val PartialContent = HttpStatus("PartialContent", 206)
  val MultiStatus = HttpStatus("MultiStatus", 207)
  val AlreadyReported = HttpStatus("AlreadyReported", 208)
  val IMUsed = HttpStatus("IMUsed", 226)

  val MultipleChoices = HttpStatus("MultipleChoices", 300)
  val MovedPermanently = HttpStatus("MovedPermanently", 301)
  val Found = HttpStatus("Found", 302)
  val SeeOther = HttpStatus("SeeOther", 303)
  val NotModified = HttpStatus("NotModified", 304)
  val UseProxy = HttpStatus("UseProxy", 305)
  val TemporaryRedirect = HttpStatus("TemporaryRedirect", 307)
  val PermanentRedirect = HttpStatus("PermanentRedirect", 308)

  val BadRequest = HttpStatus("BadRequest", 400)
  val Unauthorized = HttpStatus("Unauthorized", 401)
  val PaymentRequired = HttpStatus("PaymentRequired", 402)
  val Forbidden = HttpStatus("Forbidden", 403)
  val NotFound = HttpStatus("NotFound", 404)
  val MethodNotAllowed = HttpStatus("MethodNotAllowed", 405)
  val NotAcceptable = HttpStatus("NotAcceptable", 406)
  val ProxyAuthenticationRequired = HttpStatus("ProxyAuthenticationRequired", 407)
  val RequestTimeout = HttpStatus("RequestTimeout", 408)
  val Conflict = HttpStatus("Conflict", 409)
  val Gone = HttpStatus("Gone", 410)
  val LengthRequired = HttpStatus("LengthRequired", 411)
  val PreconditionFailed = HttpStatus("PreconditionFailed", 412)
  val PayloadTooLarge = HttpStatus("PayloadTooLarge", 413)
  val UriTooLong = HttpStatus("UriTooLong", 414)
  val UnsupportedMediaType = HttpStatus("UnsupportedMediaType", 415)
  val RangeNotSatisfiable = HttpStatus("RangeNotSatisfiable", 416)
  val ExpectationFailed = HttpStatus("ExpectationFailed", 417)
  val ImATeapot = HttpStatus("ImATeapot", 418)
  val EnhanceYourCalm = HttpStatus("EnhanceYourCalm", 420)
  val MisdirectedRequest = HttpStatus("MisdirectedRequest", 421)
  val UnprocessableEntity = HttpStatus("UnprocessableEntity", 422)
  val Locked = HttpStatus("Locked", 423)
  val FailedDependency = HttpStatus("FailedDependency", 424)
  val TooEarly = HttpStatus("TooEarly", 425)
  val UpgradeRequired = HttpStatus("UpgradeRequired", 426)
  val PreconditionRequired = HttpStatus("PreconditionRequired", 428)
  val TooManyRequests = HttpStatus("TooManyRequests", 429)
  val RequestHeaderFieldsTooLarge = HttpStatus("RequestHeaderFieldsTooLarge", 431)
  val RetryWith = HttpStatus("RetryWith", 449)
  val BlockedByParentalControls = HttpStatus("BlockedByParentalControls", 450)
  val UnavailableForLegalReasons = HttpStatus("UnavailableForLegalReasons", 451)

  val InternalServerError = HttpStatus("InternalServerError", 500)
  val NotImplemented = HttpStatus("NotImplemented", 501)
  val BadGateway = HttpStatus("BadGateway", 502)
  val ServiceUnavailable = HttpStatus("ServiceUnavailable", 503)
  val GatewayTimeout = HttpStatus("GatewayTimeout", 504)
  val HttpVersionNotSupported = HttpStatus("HttpVersionNotSupported", 505)
  val VariantAlsoNegotiates = HttpStatus("VariantAlsoNegotiates", 506)
  val InsufficientStorage = HttpStatus("InsufficientStorage", 507)
  val LoopDetected = HttpStatus("LoopDetected", 508)
  val BandwidthLimitExceeded = HttpStatus("BandwidthLimitExceeded", 509)
  val NotExtended = HttpStatus("NotExtended", 510)
  val NetworkAuthenticationRequired = HttpStatus("NetworkAuthenticationRequired", 511)
  val NetworkReadTimeout = HttpStatus("NetworkReadTimeout", 598)
  val NetworkConnectTimeout = HttpStatus("NetworkConnectTimeout", 599)

  val redirects = List(
    HttpStatuses.MultipleChoices
    , HttpStatuses.MovedPermanently
    , HttpStatuses.Found
    , HttpStatuses.SeeOther
    , HttpStatuses.NotModified
    , HttpStatuses.UseProxy
    , HttpStatuses.TemporaryRedirect
    , HttpStatuses.PermanentRedirect
  )
}

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
  def html[E <: FSXmlEnv : FSXmlSupport](ns: E#NodeSeq) = new OkHtml(implicitly[FSXmlSupport[E]].render(ns))

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

object RoutingHandlerHelper {

  trait Method

  object Method {
    def fromString: PartialFunction[String, Method] = {
      case "GET" => GET
      case "HEAD" => HEAD
      case "POST" => POST
      case "PUT" => PUT
      case "DELETE" => DELETE
      case "CONNECT" => CONNECT
      case "OPTIONS" => OPTIONS
      case "TRACE" => TRACE
      case "PATCH" => PATCH
    }
  }

  object GET extends Method

  object HEAD extends Method

  object POST extends Method

  object PUT extends Method

  object DELETE extends Method

  object CONNECT extends Method

  object OPTIONS extends Method

  object TRACE extends Method

  object PATCH extends Method

  abstract class UnapplyHelper1(methodName: String) {
    def unapplySeq(req: Request): Option[Seq[String]] =
      Some(req.getHttpURI.getPath.replaceAll("^/", "").split("/").toList.filter(_ != "")).filter(_ => req.getMethod == methodName)
  }

  object Req {

    def unapply(req: Request): Option[(Method, List[String], String, Boolean)] = {
      Method.fromString.unapply(req.getMethod).map(method => {
        val path = req.getHttpURI.getPath
        val ext = path.replaceAll(".*\\.(\\w+)$", "$1").toLowerCase
        (method,
          path.replaceAll("^/", "").replaceAll(s"\\.$ext$$", "").split("/").toList.filter(_ != ""),
          ext,
          path.endsWith("/"))
      })
    }
  }

  object Get extends UnapplyHelper1("GET")

  object Head extends UnapplyHelper1("HEAD")

  object Post extends UnapplyHelper1("POST")

  object Put extends UnapplyHelper1("PUT")

  object Delete extends UnapplyHelper1("DELETE")

  object Connect extends UnapplyHelper1("CONNECT")

  object Options extends UnapplyHelper1("OPTIONS")

  object Trace extends UnapplyHelper1("TRACE")

  object Patch extends UnapplyHelper1("PATCH")

  def onlyHandleHtmlRequests(handle: => Option[Response])(implicit req: Request): Option[Response] =
    if (Option(req.getHeaders.get(HttpHeader.ACCEPT)).getOrElse("").contains("text/html")) handle else None
}

abstract class RoutingHandlerNoSessionHelper extends Handler.Abstract {

  def handlerNoSession(response: JettyServerResponse, callback: Callback)(implicit req: Request): Option[Response]

  override def handle(request: Request, response: JettyServerResponse, callback: Callback): Boolean = {
    handlerNoSession(response, callback)(request).map(resp => {
      resp.respond(response, callback)
    }).getOrElse(false)
  }
}

abstract class RoutingHandlerHelper(implicit fss: FSSystem) extends RoutingHandlerNoSessionHelper {

  def servePage[E <: FSXmlEnv : FSXmlSupport](renderable: RenderableWithFSContext[E], debugLbl: Option[String] = None)(implicit req: Request, session: FSSession): Response = {
    session.createPage(implicit fsc => Ok.html(renderable.render())
      .addHeaders(
        "Cache-Control" -> "no-cache, max-age=0, no-store"
        , "Pragma" -> "no-cache"
        , "Expires" -> "-1"
      ), debugLbl = debugLbl.orElse(Some(req.getHttpURI.getPath))
    )
  }

  def handlerInSession(response: JettyServerResponse, callback: Callback)(implicit req: Request, session: FSSession): Option[Response]


  override def handle(request: Request, response: JettyServerResponse, callback: Callback): Boolean = {
    handlerNoSession(response, callback)(request) match {
      case Some(resp) =>
        resp.respond(response, callback)
      case None =>
        fss.inSession{
          implicit session => handlerInSession(response, callback)(request, session)
        }(request).flatMap({
          case (cookies, resp) =>
            cookies.foreach(JettyServerResponse.addCookie(response, _))
            resp.map(resp => {
              resp.respond(response, callback)
            })
        }).getOrElse(false)
    }
  }
}
