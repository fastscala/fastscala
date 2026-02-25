package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7Field, F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js

import scala.util.{Failure, Try}


trait F7FieldWithEnabled extends F7Field with Mutable {

  private val _enabled: F7FieldMixinStatus[Boolean] = F7FieldMixinStatus(true)

  def enabled: Boolean = _enabled()

  def isEnabled: this.type = enabled(true)

  def isNotEnabled: this.type = enabled(false)

  def enabled(v: Boolean): this.type = mutate {
    _enabled() = () => v
  }

  def enabled(f: () => Boolean): this.type = mutate {
    _enabled() = f
  }

  override def preRender(): Unit = {
    super.preRender()
    _enabled.setRendered()
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): Try[Js] = {
    if (_enabled.hasChanged) Failure(new Exception("Enabled status changed"))
    else super.updateFieldWithoutReRendering()
  }
}
