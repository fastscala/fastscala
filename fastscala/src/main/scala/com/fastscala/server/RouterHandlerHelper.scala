package com.fastscala.server

import com.fastscala.core.{FSSession, FSSystem}
import com.fastscala.js.Js
import com.fastscala.utils.RenderableWithFSContext
import jakarta.servlet.http.{Cookie, HttpServletRequest, HttpServletResponse}
import org.apache.commons.io.IOUtils
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler

import java.io.File
import java.nio.file.Files
import scala.xml.NodeSeq

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

  val cookiesToAdd = collection.mutable.ListBuffer[Cookie]()

  def addHeader(name: String, value: String): this.type = {
    headers += ((name, value))
    this
  }

  def addHeaders(headers: (String, String)*): this.type = {
    headers.foreach({
      case (name, value) => addHeader(name, value)
    })
    this
  }

  def respond(response: HttpServletResponse): Unit = {
    response.setStatus(status.code)
    headers.foreach({
      case (name, value) => response.setHeader(name, value)
    })
    cookiesToAdd.foreach(response.addCookie)
  }
}

trait TextResponse extends Response {
  def contents: String

  def contentType: String

  override def respond(response: HttpServletResponse): Unit = {
    response.setContentType(contentType)
    super.respond(response)
    response.getWriter.write(contents)
  }
}

trait BinaryResponse extends Response {
  def contents: Array[Byte]

  def contentType: String

  override def respond(response: HttpServletResponse): Unit = {
    super.respond(response)
    response.setContentType(contentType)
    IOUtils.write(contents, response.getOutputStream)
  }
}

trait RedirectResponse extends TextResponse {

  def redirectTo: String

  def contentType: String = "text/css;charset=utf-8"

  override def contents: String =
    <html>
    <head><title>Moved</title></head>
    <body>=Moved=<p>This page has moved to <a href={redirectTo}>{redirectTo}</a>.</p></body>
    </html>.toString

  override def respond(response: HttpServletResponse): Unit = {
    super.respond(response)
    response.setContentType(contentType)
    response.setHeader("Location", redirectTo)
  }
}

object Ok {
  def html(ns: NodeSeq) = new OkHtml(ns)

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

    override def contentType: String = Files.probeContentType(new File(fileName).toPath())

    override def status: HttpStatus = HttpStatuses.OK
  }

  def binaryAutoDetectContentType(bytes: Array[Byte], fileName: String) = new BinaryResponse {
    override def contents: Array[Byte] = bytes

    override def contentType: String = Files.probeContentType(new File(fileName).toPath())

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
  def apply(httpStatus: HttpStatus) = new Response {
    override def status: HttpStatus = httpStatus
  }

  def BadRequest = apply(HttpStatuses.BadRequest)

  def Unauthorized = apply(HttpStatuses.Unauthorized)

  def PaymentRequired = apply(HttpStatuses.PaymentRequired)

  def Forbidden = apply(HttpStatuses.Forbidden)

  def NotFound = apply(HttpStatuses.NotFound)

  def MethodNotAllowed = apply(HttpStatuses.MethodNotAllowed)

  def NotAcceptable = apply(HttpStatuses.NotAcceptable)

  def ProxyAuthenticationRequired = apply(HttpStatuses.ProxyAuthenticationRequired)

  def RequestTimeout = apply(HttpStatuses.RequestTimeout)

  def Conflict = apply(HttpStatuses.Conflict)

  def Gone = apply(HttpStatuses.Gone)

  def LengthRequired = apply(HttpStatuses.LengthRequired)

  def PreconditionFailed = apply(HttpStatuses.PreconditionFailed)

  def PayloadTooLarge = apply(HttpStatuses.PayloadTooLarge)

  def UriTooLong = apply(HttpStatuses.UriTooLong)

  def UnsupportedMediaType = apply(HttpStatuses.UnsupportedMediaType)

  def RangeNotSatisfiable = apply(HttpStatuses.RangeNotSatisfiable)

  def ExpectationFailed = apply(HttpStatuses.ExpectationFailed)

  def ImATeapot = apply(HttpStatuses.ImATeapot)

  def EnhanceYourCalm = apply(HttpStatuses.EnhanceYourCalm)

  def MisdirectedRequest = apply(HttpStatuses.MisdirectedRequest)

  def UnprocessableEntity = apply(HttpStatuses.UnprocessableEntity)

  def Locked = apply(HttpStatuses.Locked)

  def FailedDependency = apply(HttpStatuses.FailedDependency)

  def TooEarly = apply(HttpStatuses.TooEarly)

  def UpgradeRequired = apply(HttpStatuses.UpgradeRequired)

  def PreconditionRequired = apply(HttpStatuses.PreconditionRequired)

  def TooManyRequests = apply(HttpStatuses.TooManyRequests)

  def RequestHeaderFieldsTooLarge = apply(HttpStatuses.RequestHeaderFieldsTooLarge)

  def RetryWith = apply(HttpStatuses.RetryWith)

  def BlockedByParentalControls = apply(HttpStatuses.BlockedByParentalControls)

  def UnavailableForLegalReasons = apply(HttpStatuses.UnavailableForLegalReasons)
}

object ServerError {
  def apply(httpStatus: HttpStatus) = new Response {
    override def status: HttpStatus = httpStatus
  }

  def InternalServerError = apply(HttpStatuses.InternalServerError)

  def NotImplemented = apply(HttpStatuses.NotImplemented)

  def BadGateway = apply(HttpStatuses.BadGateway)

  def ServiceUnavailable = apply(HttpStatuses.ServiceUnavailable)

  def GatewayTimeout = apply(HttpStatuses.GatewayTimeout)

  def HttpVersionNotSupported = apply(HttpStatuses.HttpVersionNotSupported)

  def VariantAlsoNegotiates = apply(HttpStatuses.VariantAlsoNegotiates)

  def InsufficientStorage = apply(HttpStatuses.InsufficientStorage)

  def LoopDetected = apply(HttpStatuses.LoopDetected)

  def BandwidthLimitExceeded = apply(HttpStatuses.BandwidthLimitExceeded)

  def NotExtended = apply(HttpStatuses.NotExtended)

  def NetworkAuthenticationRequired = apply(HttpStatuses.NetworkAuthenticationRequired)

  def NetworkReadTimeout = apply(HttpStatuses.NetworkReadTimeout)

  def NetworkConnectTimeout = apply(HttpStatuses.NetworkConnectTimeout)
}

class OkHtml(ns: NodeSeq) extends Response with TextResponse {
  override def status: HttpStatus = HttpStatuses.OK

  override def contentType: String = "text/html;charset=utf-8"

  def docType: String = "<!DOCTYPE html>"

  override def contents: String = docType + "\n" + ns.toString()
}

class OkTextBased(text: String, val contentType: String) extends TextResponse {
  override def status: HttpStatus = HttpStatuses.OK

  override def contents: String = text
}


abstract class RouterHandlerHelper extends AbstractHandler {

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
    //    def unapply(req: HttpServletRequest): Option[List[String]] = {
    //      Some(req.getRequestURI.replaceAll("^/", "").split("/").toList.filter(_ != "")).filter(_ => req.getMethod == methodName)
    //    }

    def unapplySeq(req: HttpServletRequest): Option[Seq[String]] =
      Some(req.getRequestURI.replaceAll("^/", "").split("/").toList.filter(_ != "")).filter(_ => req.getMethod == methodName)
  }

  object Req {

    def unapply(req: HttpServletRequest): Option[(Method, List[String], String, Boolean)] = {
      Method.fromString.unapply(req.getMethod).map(method => {
        val ext = req.getRequestURI.replaceAll(".*\\.(\\w+)$", "$1").toLowerCase
        (method,
          req.getRequestURI.replaceAll("^/", "").replaceAll(s"\\.$ext$$", "").split("/").toList.filter(_ != ""),
          ext,
          req.getRequestURI.endsWith("/"))
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

  def handlerNoSession(implicit req: HttpServletRequest): Option[Response]

  override def handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse): Unit = {
    handlerNoSession(request).foreach(resp => {
      resp.respond(response)
      baseRequest.setHandled(true)
    })
  }
}

abstract class MainRouterHandlerHelper(implicit fss: FSSystem) extends RouterHandlerHelper {

  def servePage(renderable: RenderableWithFSContext, debugLbl: Option[String] = None)(implicit req: HttpServletRequest, session: FSSession): Response = {
    session.createPage(implicit fsc => Ok.html(renderable.render())
      .addHeaders(
        "Cache-Control" -> "no-cache, max-age=0, no-store"
        , "Pragma" -> "no-cache"
        , "Expires" -> "-1"
      ), debugLbl = debugLbl.orElse(Some(req.getRequestURI))
    )
  }

  def handlerInSession(implicit req: HttpServletRequest, session: FSSession): Option[Response]

  override def handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse): Unit = {
    handlerNoSession(request) match {
      case Some(resp) =>
        resp.respond(response)
        baseRequest.setHandled(true)
      case None =>
        fss.inSession[Option[Response]]({
          implicit session => handlerInSession(request, session)
        })(request).foreach({
          case (cookies, resp) =>
            cookies.foreach(cookie => {
              response.addCookie(cookie)
            })
            resp.foreach(resp => {
              resp.respond(response)
              baseRequest.setHandled(true)
            })
        })
    }
  }
}
