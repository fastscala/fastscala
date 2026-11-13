package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7Field, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js

import scala.util.{Failure, Try}

/**
 * The size a field is rendered with. How each size looks is up to the renderer: a renderer that has fewer sizes maps
 * the others to the closest one (the Bootstrap 5 renderers render ExtraSmall as Small and ExtraLarge as Large).
 */
object InputSize extends Enumeration {
  val ExtraSmall = Value("ExtraSmall")
  val Small = Value("Small")
  val Default = Value("Default")
  val Large = Value("Large")
  val ExtraLarge = Value("ExtraLarge")
}

/**
 * A field that can be rendered in one of the [[InputSize]]s.
 *
 * (Not to be confused with [[com.fastscala.components.form7.mixins.mainelem.F7FieldWithSize]], which sets the `size`
 * attribute of the main element.)
 */
trait F7FieldWithInputSize extends F7Field with Mutable {

  private val _inputSize = F7FieldMixinStatus(InputSize.Default)

  def inputSize: InputSize.Value = _inputSize()

  def inputSize(v: InputSize.Value): this.type = mutate {
    _inputSize() = () => v
  }

  def inputSize(f: () => InputSize.Value): this.type = mutate {
    _inputSize() = f
  }

  def inputSizeXs: this.type = inputSize(InputSize.ExtraSmall)

  def inputSizeSm: this.type = inputSize(InputSize.Small)

  def inputSizeDefault: this.type = inputSize(InputSize.Default)

  def inputSizeLg: this.type = inputSize(InputSize.Large)

  def inputSizeXl: this.type = inputSize(InputSize.ExtraLarge)

  // The classes for each size depend on the renderer, so a change of input size rerenders the field.
  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): Try[Js] =
    if (_inputSize.hasChanged) Failure(new Exception("Input size changed: need to rerender"))
    else super.updateFieldWithoutReRendering()
}
