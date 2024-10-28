package com.fastscala.templates.form7.mixins

import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithTabIndex extends F7FieldInputFieldMixin {
  var _tabIndex: () => Option[Int] = () => None

  def tabIndex: Option[Int] = _tabIndex()

  def tabIndex(v: Option[Int]): this.type = mutate {
    _tabIndex = () => v
  }

  def tabIndex(v: Int): this.type = mutate {
    _tabIndex = () => Some(v)
  }

  def tabIndex(f: () => Option[Int]): this.type = mutate {
    _tabIndex = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _tabIndex().map(tabIndex => input.withAttr("tabindex", tabIndex.toString)).getOrElse(input)
  }
}
