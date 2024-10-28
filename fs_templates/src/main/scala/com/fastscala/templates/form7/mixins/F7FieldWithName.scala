package com.fastscala.templates.form7.mixins

import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithName extends F7FieldInputFieldMixin {
  var _name: () => Option[String] = () => None

  def name(): Option[String] = _name()

  def name(v: String): this.type = mutate {
    _name = () => Some(v)
  }

  def name(v: Option[String]): this.type = mutate {
    _name = () => v
  }

  def name(f: () => Option[String]): this.type = mutate {
    _name = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _name().map(name => input.withAttr("name", name)).getOrElse(input)
  }
}
