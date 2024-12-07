package com.fastscala.components.form7.mixins

import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithRequired extends F7FieldInputFieldMixin {
  var _required: () => Boolean = () => false

  def required: Boolean = _required()

  def isRequired: this.type = required(true)

  def isNotRequired: this.type = required(false)

  def required(v: Boolean): this.type = mutate {
    _required = () => v
  }

  def required(f: () => Boolean): this.type = mutate {
    _required = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    if (_required()) input.withAttr("required", "true") else input
  }
}
