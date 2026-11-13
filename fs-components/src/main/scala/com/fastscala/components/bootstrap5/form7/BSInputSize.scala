package com.fastscala.components.bootstrap5.form7

import com.fastscala.components.form7.mixins.{InputSize, F7FieldWithInputSize}

/**
 * The Bootstrap 5 classes for each [[InputSize]]. Bootstrap only has small and large form controls and input groups,
 * so ExtraSmall is rendered as Small and ExtraLarge as Large.
 */
object BSInputSize {

  /** The class for a `.form-control` element of the given size, if any. */
  def formControlClass(size: InputSize.Value): Option[String] = size match {
    case InputSize.ExtraSmall | InputSize.Small => Some("form-control-sm")
    case InputSize.Default => None
    case InputSize.Large | InputSize.ExtraLarge => Some("form-control-lg")
  }

  /** The class for an `.input-group` element of the given size, if any. */
  def inputGroupClass(size: InputSize.Value): Option[String] = size match {
    case InputSize.ExtraSmall | InputSize.Small => Some("input-group-sm")
    case InputSize.Default => None
    case InputSize.Large | InputSize.ExtraLarge => Some("input-group-lg")
  }

  /** The class for the `.form-control` element of `field`, if it has a size other than the default. */
  def formControlClassOf(field: Any): Option[String] = field match {
    case field: F7FieldWithInputSize => formControlClass(field.inputSize)
    case _ => None
  }
}
