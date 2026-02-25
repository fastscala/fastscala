package com.fastscala.components.form7.mixins

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.form7.{F7FieldMixinStatus, Form7, RenderHint}
import com.fastscala.components.utils.Mutable
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithDisabled extends F7FieldInputFieldMixin with Mutable {
  private val _disabled: F7FieldMixinStatus[Boolean] = F7FieldMixinStatus(false)

  def disabled: Boolean = _disabled()

  def isDisabled: this.type = disabled(true)

  def isNotDisabled: this.type = disabled(false)

  def disabled(v: Boolean): this.type = mutate {
    _disabled() = () => v
  }

  def disabled(f: () => Boolean): this.type = mutate({
    _disabled() = f
  })

  override def preRender(): Unit = {
    super.preRender()
    _disabled.setRendered()
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    if (_disabled()) input.withAttr("disabled", "disabled") else input
  }

  def updateFieldDisabledStatus(_disabled: F7FieldMixinStatus[Boolean])(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] = scala.util.Success(_disabled.updateIfChanged({
    case (_, true) => JS.setAttr(elemId)("disabled", "disabled")
    case (_, false) => JS.removeAttr(elemId, "disabled")
  }, Js.Void))

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().flatMap(js => updateFieldDisabledStatus(_disabled).map(js & _))
}
