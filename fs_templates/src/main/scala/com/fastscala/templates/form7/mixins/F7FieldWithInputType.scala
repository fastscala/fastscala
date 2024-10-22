package com.fastscala.templates.form7.mixins

import com.fastscala.templates.form7.fields.text.F7FieldInputFieldMixin
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem

trait F7FieldWithInputType extends F7FieldInputFieldMixin {
  def _inputTypeDefault: String = "text"

  var _inputType: () => String = () => _inputTypeDefault

  def inputType: String = _inputType()

  def inputType(v: String): this.type = mutate {
    _inputType = () => v
  }

  def inputType(f: () => String): this.type = mutate {
    _inputType = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    input.withAttr("type", _inputType())
  }
}
