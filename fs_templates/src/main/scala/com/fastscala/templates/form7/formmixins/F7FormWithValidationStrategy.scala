package com.fastscala.templates.form7.formmixins

import com.fastscala.templates.form7.F7FormValidationStrategy
import com.fastscala.templates.utils.Mutable


trait F7FormWithValidationStrategy extends Mutable {
  var _validationStrategy: () => F7FormValidationStrategy.Value = () => F7FormValidationStrategy.ValidateOnAttemptSubmitOnly

  def validationStrategy: F7FormValidationStrategy.Value = _validationStrategy()

  def validateBeforeUserInput(): this.type = mutate {
    _validationStrategy = () => F7FormValidationStrategy.ValidateBeforeUserInput
  }

  def validateEachFieldAfterUserInput(): this.type = mutate {
    _validationStrategy = () => F7FormValidationStrategy.ValidateEachFieldAfterUserInput
  }

  def validateOnAttemptSubmitOnly(): this.type = mutate {
    _validationStrategy = () => F7FormValidationStrategy.ValidateOnAttemptSubmitOnly
  }

  def validationStrategy(f: () => F7FormValidationStrategy.Value): this.type = mutate {
    _validationStrategy = f
  }
}