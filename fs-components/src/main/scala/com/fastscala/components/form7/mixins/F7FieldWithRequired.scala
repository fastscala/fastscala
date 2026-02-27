package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithRequired extends F7FieldInputFieldMixin with Mutable {
  private val _required: F7FieldMixinStatus[Boolean] = F7FieldMixinStatus(false)

  def required: Boolean = _required()

  def isRequired: this.type = required(true)

  def isNotRequired: this.type = required(false)

  def required(v: Boolean): this.type = mutate {
    _required() = () => v
  }

  def required(f: () => Boolean): this.type = mutate {
    _required() = f
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ & _required.updateIfChanged({
      case (_, true) => JS.setAttr(elemId)("required", "true")
      case (_, false) => JS.removeAttr(elemId, "required")
    }, Js.Void))
}
