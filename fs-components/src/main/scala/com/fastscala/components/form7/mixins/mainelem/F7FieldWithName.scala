package com.fastscala.components.form7.mixins.mainelem

import com.fastscala.components.form7.{F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithName extends F7FieldWithMainElem with Mutable {
  private val _name: F7FieldMixinStatus[Option[String]] = F7FieldMixinStatus(None)

  def name(): Option[String] = _name()

  def name(v: String): this.type = mutate {
    _name() = () => Some(v)
  }

  def name(v: Option[String]): this.type = mutate {
    _name() = () => v
  }

  def name(f: () => Option[String]): this.type = mutate {
    _name() = f
  }

  override def processMainElem(input: Elem): Elem = super.processMainElem(input).pipe { input =>
    _name().map(name => input.withAttr("name", name)).getOrElse(input)
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ & _name.updateIfChanged({
      case (old, None) => JS.removeAttr(mainElemId, "name")
      case (old, Some(value)) => JS.setAttr(mainElemId)("name", value)
    }, Js.Void))
}
