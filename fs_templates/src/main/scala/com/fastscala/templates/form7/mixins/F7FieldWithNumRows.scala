package com.fastscala.templates.form7.mixins

import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithNumRows extends F7FieldInputFieldMixin {
  var _rows: () => Option[Int] = () => None

  def rows: Option[Int] = _rows()

  def rows(v: Option[Int]): this.type = mutate {
    _rows = () => v
  }

  def rows(v: Int): this.type = mutate {
    _rows = () => Some(v)
  }

  def rows(f: () => Option[Int]): this.type = mutate {
    _rows = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>

    _rows().map(rows => input.withAttr("rows", rows.toString)).getOrElse(input)
  }
}
