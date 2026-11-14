package com.fastscala.routing.resp


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
