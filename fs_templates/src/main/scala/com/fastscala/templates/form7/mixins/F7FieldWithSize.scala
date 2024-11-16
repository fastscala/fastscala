package com.fastscala.templates.form7.mixins

import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithSize extends F7FieldInputFieldMixin {
  var _size: () => Option[Int] = () => None

  def size(): Option[Int] = _size()

  def size(v: Int): this.type = mutate {
    _size = () => Some(v)
  }

  def size(v: Option[Int]): this.type = mutate {
    _size = () => v
  }

  def size(f: () => Option[Int]): this.type = mutate {
    _size = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _size().map(size => input.withAttr("size", size.toString)).getOrElse(input)
  }
}
