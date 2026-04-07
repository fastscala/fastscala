package com.fastscala.components.form7.mixins.mainelem

import com.fastscala.components.form7.mixins.mainelem.F7FieldWithMainElem
import com.fastscala.components.form7.{F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem

trait F7FieldWithInputElemType extends F7FieldWithMainElem with Mutable {
  def _inputTypeDefault: String = "text"

  private val _inputType: F7FieldMixinStatus[String] = F7FieldMixinStatus(_inputTypeDefault)

  def inputType: String = _inputType()

  def inputType(v: String): this.type = mutate {
    _inputType() = () => v
  }

  def inputType(f: () => String): this.type = mutate {
    _inputType() = f
  }

  override def processMainElem(input: Elem): Elem = super.processMainElem(input).pipe { input =>
    input.withAttr("type", _inputType())
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ & _inputType.updateIfChanged({
      case (old, cur) => JS.setAttr(mainElemId)("type", cur)
    }, Js.Void))

  def inputTypeEmail: this.type = mutate {
    inputType("email")
  }

  def inputTypePassword: this.type = mutate {
    inputType("password")
  }

  def inputTypeTel: this.type = mutate {
    inputType("tel")
  }

  def inputTypeUrl: this.type = mutate {
    inputType("url")
  }
}
