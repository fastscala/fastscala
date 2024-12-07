package com.fastscala.components.bootstrap5.form7.mixins

import com.fastscala.components.form7.F7Field

object InputGroupSize extends Enumeration {
  val Small = Value("Small")
  val Default = Value("Default")
  val Large = Value("Large")

  implicit class RichValue(v: Value) {
    def `class` = v match {
      case Small => "input-group-sm"
      case Default => ""
      case Large => "input-group-lg"
    }
  }
}

trait F7FieldWithInputGroupSize extends F7Field {
  var _inputGroupSize: () => InputGroupSize.Value = () => InputGroupSize.Default

  def inputGroupSize: InputGroupSize.Value = _inputGroupSize()

  def inputGroupSizeSmall(): this.type = inputGroupSize(InputGroupSize.Small)

  def inputGroupSizeLarge(): this.type = inputGroupSize(InputGroupSize.Large)

  def inputGroupSize(v: InputGroupSize.Value): this.type = mutate {
    _inputGroupSize = () => v
  }
}
