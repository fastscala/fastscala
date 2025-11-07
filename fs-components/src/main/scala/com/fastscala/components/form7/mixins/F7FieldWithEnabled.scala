package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7Field, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js

import scala.util.{Failure, Try}


trait F7FieldWithEnabled extends F7Field {

  var _enabled: () => Boolean = () => true

  def enabled: Boolean = _enabled()

  def isEnabled: this.type = enabled(true)

  def isNotEnabled: this.type = enabled(false)

  def enabled(v: Boolean): this.type = mutate {
    _enabled = () => v
  }

  def enabled(f: () => Boolean): this.type = mutate {
    _enabled = f
  }

  protected var currentRenderedAsEnabled = Option.empty[Boolean]

  override def renderedFieldWithCurrentState(): Unit = {
    super.renderedFieldWithCurrentState()
    currentRenderedAsEnabled = Some(enabled)
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): Try[Js] = {
    if (!currentRenderedAsEnabled.forall(_ == enabled)) Failure(new Exception("Enabled status changed"))
    else super.updateFieldWithoutReRendering()
  }
}
