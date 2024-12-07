package com.fastscala.components.form7

object F7FormValidationStrategy extends Enumeration {

  val ValidateBeforeUserInput = Value
  val ValidateEachFieldAfterUserInput = Value
  val ValidateOnAttemptSubmitOnly = Value
}
