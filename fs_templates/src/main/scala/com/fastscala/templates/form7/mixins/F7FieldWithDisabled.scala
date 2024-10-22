package com.fastscala.templates.form7.mixins

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7.fields.text.F7FieldInputFieldMixin
import com.fastscala.templates.form7.{Form7, RenderHint}
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

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

  def updateFieldDisabledStatus()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = _disabled().pipe(shouldBeDisabled => {
    if (shouldBeDisabled != currentlyDisabled) {
      currentlyDisabled = shouldBeDisabled
      if (currentlyDisabled) {
        Js.setAttr(elemId)("disabled", "disabled")
      } else {
        Js.removeAttr(elemId, "disabled")
      }
    } else {
      Js.void
    }
  })

  override def updateFieldStatus()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js =
    super.updateFieldStatus() & updateFieldDisabledStatus()
}
