package com.fastscala.templates.bootstrap5.form7.renderermodifiers

object CheckboxStyle extends Enumeration {

  val Checkbox = Value
  val Switch = Value

  implicit def default: Value = Checkbox
}
