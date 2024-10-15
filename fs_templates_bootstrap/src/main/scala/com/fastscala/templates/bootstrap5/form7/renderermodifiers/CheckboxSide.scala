package com.fastscala.templates.bootstrap5.form7.renderermodifiers

object CheckboxSide extends Enumeration {

  val Normal = Value
  val Opposite = Value

  implicit def default: Value = Normal
}
