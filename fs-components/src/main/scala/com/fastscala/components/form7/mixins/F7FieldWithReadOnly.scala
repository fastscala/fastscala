package com.fastscala.components.form7.mixins

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.form7.{F7FieldMixinStatus, Form7, RenderHint}
import com.fastscala.components.utils.Mutable
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithReadOnly extends F7FieldInputFieldMixin with Mutable {
  private val _readOnly: F7FieldMixinStatus[Boolean] = F7FieldMixinStatus(false)

  def readOnly: Boolean = _readOnly()

  def isReadOnly: this.type = readOnly(true)

  def isNotReadOnly: this.type = readOnly(false)

  def readOnly(v: Boolean): this.type = mutate {
    _readOnly() = () => v
  }

  def readOnly(f: () => Boolean): this.type = mutate {
    _readOnly() = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    if (_readOnly()) input.withAttr("readonly", "true") else input
  }

  override def preRender(): Unit = {
    super.preRender()
    _readOnly.setRendered()
  }

  def updateFieldReadOnlyStatus(_readOnly: F7FieldMixinStatus[Boolean])(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] = scala.util.Success(_readOnly.updateIfChanged({
    case (_, true) => JS.setAttr(elemId)("readonly", "readonly")
    case (_, false) => JS.removeAttr(elemId, "readonly")
  }, Js.Void))

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().flatMap(js => updateFieldReadOnlyStatus(_readOnly).map(js & _))
}
