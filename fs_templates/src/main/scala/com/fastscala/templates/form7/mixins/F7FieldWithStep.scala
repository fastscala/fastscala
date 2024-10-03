package com.fastscala.templates.form7.mixins

import com.fastscala.templates.form7.fields.text.F7FieldInputFieldMixin
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithStep extends F7FieldInputFieldMixin {
  var _step: () => Option[Int] = () => None

  def step(): Option[Int] = _step()

  def step(v: Option[Int]): this.type = mutate {
    _step = () => v
  }

  def step(v: Int): this.type = mutate {
    _step = () => Some(v)
  }

  def step(f: () => Option[Int]): this.type = mutate {
    _step = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _step().map(step => input.withAttr("step", step.toString)).getOrElse(input)
  }
}
