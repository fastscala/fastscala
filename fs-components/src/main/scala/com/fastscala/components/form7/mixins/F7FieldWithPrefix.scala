package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7Field, F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js


trait F7FieldWithPrefix extends F7Field with Mutable {

  private val _prefix: F7FieldMixinStatus[String] = F7FieldMixinStatus("")

  def prefix: String = _prefix()

  def prefix(v: String): this.type = mutate {
    _prefix() = () => v
  }

  def prefix(f: () => String): this.type = mutate {
    _prefix() = f
  }

  override def preRender(): Unit = {
    super.preRender()
    _prefix.setRendered()
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    if (_prefix.hasChanged) scala.util.Failure(new Exception("Need to rerender"))
    else super.updateFieldWithoutReRendering()
}
