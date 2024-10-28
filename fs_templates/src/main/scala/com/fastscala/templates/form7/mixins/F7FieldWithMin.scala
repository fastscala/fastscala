package com.fastscala.templates.form7.mixins

import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithMin extends F7FieldInputFieldMixin {
  var _min: () => Option[String] = () => None

  def min: Option[String] = _min()

  def min(v: Option[String]): this.type = mutate {
    _min = () => v
  }

  def min(v: String): this.type = mutate {
    _min = () => Some(v)
  }

  def min(f: () => Option[String]): this.type = mutate {
    _min = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _min().map(min => input.withAttr("min", min)).getOrElse(input)
  }
}
