package com.fastscala.components.bootstrap5.form7.renderermodifiers

object CheckboxAlignment extends Enumeration {

  val Vertical = Value
  val Horizontal = Value

  implicit def default: Value = Vertical
}
