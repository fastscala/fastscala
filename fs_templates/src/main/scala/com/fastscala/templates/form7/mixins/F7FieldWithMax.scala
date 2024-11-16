package com.fastscala.templates.form7.mixins

import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithMax extends F7FieldInputFieldMixin {
  var _max: () => Option[String] = () => None

  def max: Option[String] = _max()

  def max(v: Option[String]): this.type = mutate {
    _max = () => v
  }

  def max(v: String): this.type = mutate {
    _max = () => Some(v)
  }

  def max(f: () => Option[String]): this.type = mutate {
    _max = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _max().map(max => input.withAttr("max", max)).getOrElse(input)
  }
}
