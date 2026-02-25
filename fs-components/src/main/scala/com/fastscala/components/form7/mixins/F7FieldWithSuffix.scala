package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7Field, F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js


trait F7FieldWithSuffix extends F7Field with Mutable {

  var _suffix: F7FieldMixinStatus[String] = F7FieldMixinStatus("")

  def suffix: String = _suffix()

  def suffix(v: String): this.type = mutate {
    _suffix() = () => v
  }

  def suffix(f: () => String): this.type = mutate {
    _suffix() = f
  }

  override def preRender(): Unit = {
    super.preRender()
    _suffix.setRendered()
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    if (_suffix.hasChanged) scala.util.Failure(new Exception("Need to rerender"))
    else super.updateFieldWithoutReRendering()
}
