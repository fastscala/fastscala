package com.fastscala.templates.form7.mixins

import com.fastscala.templates.form7.fields.text.F7FieldInputFieldMixin
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithDisabled extends F7FieldInputFieldMixin {
  var _disabled: () => Boolean = () => false

  def disabled() = _disabled()

  def isDisabled: this.type = disabled(true)

  def isNotDisabled: this.type = disabled(false)

  def disabled(v: Boolean): this.type = mutate {
    _disabled = () => v
  }

  def disabled(f: () => Boolean): this.type = mutate({
    _disabled = f
  })

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    if (_disabled()) input.withAttr("disabled", "disabled") else input
  }
}
