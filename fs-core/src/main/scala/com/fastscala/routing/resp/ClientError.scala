package com.fastscala.routing.resp

import org.eclipse.jetty.server.Response as JettyServerResponse
import org.eclipse.jetty.util.Callback

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
