package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithMin extends F7FieldInputFieldMixin with Mutable {
  private val _min: F7FieldMixinStatus[Option[String]] = F7FieldMixinStatus(None)

  def min: Option[String] = _min()

  def min(v: Option[String]): this.type = mutate {
    _min() = () => v
  }

  def min(v: String): this.type = mutate {
    _min() = () => Some(v)
  }

  def min(f: () => Option[String]): this.type = mutate {
    _min() = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _min().map(min => input.withAttr("min", min)).getOrElse(input)
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ & _min.updateIfChanged({
      case (old, None) => JS.removeAttr(elemId, "min")
      case (old, Some(value)) => JS.setAttr(elemId)("min", value)
    }, Js.Void))
}
