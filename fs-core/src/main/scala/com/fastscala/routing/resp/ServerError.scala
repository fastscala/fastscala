package com.fastscala.routing.resp

import org.eclipse.jetty.server.Response as JettyServerResponse
import org.eclipse.jetty.util.Callback

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
