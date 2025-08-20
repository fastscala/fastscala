package com.fastscala.components.form7.formmixins

import com.fastscala.components.form7.Form7State
import com.fastscala.components.utils.Mutable

trait F7FormWithInitialState extends Mutable {
  var _initialState: () => Form7State.Value = () => Form7State.Filling

  def initialState: Form7State.Value = _initialState()

  def initialStateFilling(): this.type = mutate {
    _initialState = () => Form7State.Filling
  }

  def initialStateValidationFailed(): this.type = mutate {
    _initialState = () => Form7State.ValidationFailed
  }

  def initialStateSaved(): this.type = mutate {
    _initialState = () => Form7State.Saved
  }

  def initialState(f: () => Form7State.Value): this.type = mutate {
    _initialState = f
  }
}
