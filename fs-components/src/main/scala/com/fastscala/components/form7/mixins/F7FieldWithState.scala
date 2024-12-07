package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.F7FieldState
import com.fastscala.components.utils.Mutable


trait F7FieldWithState extends Mutable {

  var _state: F7FieldState.Value = F7FieldState.AwaitingInput

  def state: F7FieldState.Value = _state

  def isAwaitingInput_? = state == F7FieldState.AwaitingInput

  def setAwaitingInput() = mutate {
    _state = F7FieldState.AwaitingInput
  }

  def isFilled_? = state == F7FieldState.Filled

  def setFilled() = mutate {
    _state = F7FieldState.Filled
  }

  def state(state: F7FieldState.Value): this.type = mutate {
    _state = state
  }
}
