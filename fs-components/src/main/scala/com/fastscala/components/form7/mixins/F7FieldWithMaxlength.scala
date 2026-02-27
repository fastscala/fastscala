package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithMaxlength extends F7FieldInputFieldMixin with Mutable {
  private val _maxlength: F7FieldMixinStatus[Option[Int]] = F7FieldMixinStatus(None)

  def maxlength: Option[Int] = _maxlength()

  def maxlength(v: Option[Int]): this.type = mutate {
    _maxlength() = () => v
  }

  def maxlength(v: Int): this.type = mutate {
    _maxlength() = () => Some(v)
  }

  def maxlength(f: () => Option[Int]): this.type = mutate {
    _maxlength() = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _maxlength().map(maxlength => input.withAttr("maxlength", maxlength.toString)).getOrElse(input)
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ & _maxlength.updateIfChanged({
      case (old, None) => JS.removeAttr(elemId, "maxlength")
      case (old, Some(value)) => JS.setAttr(elemId)("maxlength", value.toString)
    }, Js.Void))
}
