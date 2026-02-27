package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem

trait F7FieldWithInputType extends F7FieldInputFieldMixin with Mutable {
  def _inputTypeDefault: String = "text"

  private val _inputType: F7FieldMixinStatus[String] = F7FieldMixinStatus(_inputTypeDefault)

  def inputType: String = _inputType()

  def inputType(v: String): this.type = mutate {
    _inputType() = () => v
  }

  def inputType(f: () => String): this.type = mutate {
    _inputType() = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    input.withAttr("type", _inputType())
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ & _inputType.updateIfChanged({
      case (old, cur) => JS.setAttr(elemId)("type", cur)
    }, Js.Void))
}
