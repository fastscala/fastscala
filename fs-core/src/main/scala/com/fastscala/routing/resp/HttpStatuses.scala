package com.fastscala.routing.resp


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