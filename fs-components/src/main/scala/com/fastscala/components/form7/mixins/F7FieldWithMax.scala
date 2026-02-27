package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithMax extends F7FieldInputFieldMixin with Mutable {
  private val _max: F7FieldMixinStatus[Option[String]] = F7FieldMixinStatus(None)

  def max: Option[String] = _max()

  def max(v: Option[String]): this.type = mutate {
    _max() = () => v
  }

  def max(v: String): this.type = mutate {
    _max() = () => Some(v)
  }

  def max(f: () => Option[String]): this.type = mutate {
    _max() = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _max().map(max => input.withAttr("max", max)).getOrElse(input)
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ & _max.updateIfChanged({
      case (old, None) => JS.removeAttr(elemId, "max")
      case (old, Some(value)) => JS.setAttr(elemId)("max", value)
    }, Js.Void))
}
