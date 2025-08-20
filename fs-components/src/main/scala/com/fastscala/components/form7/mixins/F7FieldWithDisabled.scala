package com.fastscala.components.form7.mixins

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.form7.{Form7, RenderHint}
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithDisabled extends F7FieldInputFieldMixin {
  var _disabled: () => Boolean = () => false

  def disabled: Boolean = _disabled()

  def isDisabled: this.type = disabled(true)

  def isNotDisabled: this.type = disabled(false)

  def disabled(v: Boolean): this.type = mutate {
    _disabled = () => v
  }

  def disabled(f: () => Boolean): this.type = mutate({
    _disabled = f
  })

  var currentlyDisabled: Boolean = false

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    currentlyDisabled = _disabled()
    if (currentlyDisabled) input.withAttr("disabled", "disabled") else input
  }

  def updateFieldDisabledStatus()(implicit form: Form7, fsc: FSContext): Js = _disabled().pipe(shouldBeDisabled => {
    if (shouldBeDisabled != currentlyDisabled) {
      currentlyDisabled = shouldBeDisabled
      if (currentlyDisabled) {
        JS.setAttr(elemId)("disabled", "disabled")
      } else {
        JS.removeAttr(elemId, "disabled")
      }
    } else {
      JS.void
    }
  })

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ & updateFieldDisabledStatus())
}
