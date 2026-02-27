package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithPlaceholder extends F7FieldInputFieldMixin with Mutable {
  private val _placeholder: F7FieldMixinStatus[Option[String]] = F7FieldMixinStatus(None)

  def placeholder: Option[String] = _placeholder()

  def placeholder(v: Option[String]): this.type = mutate {
    _placeholder() = () => v
  }

  def placeholder(v: String): this.type = mutate {
    _placeholder() = () => Some(v)
  }

  def placeholder(f: () => Option[String]): this.type = mutate {
    _placeholder() = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _placeholder().map(placeholder => input.withAttr("placeholder", placeholder)).getOrElse(input)
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ & _placeholder.updateIfChanged({
      case (old, None) => JS.removeAttr(elemId, "placeholder")
      case (old, Some(value)) => JS.setAttr(elemId)("placeholder", value)
    }, Js.Void))
}
