package com.fastscala.components.form7.mixins

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.form7.{Form7, RenderHint}
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithReadOnly extends F7FieldInputFieldMixin {
  var _readOnly: () => Boolean = () => false

  def readOnly: Boolean = _readOnly()

  def isReadOnly: this.type = readOnly(true)

  def isNotReadOnly: this.type = readOnly(false)

  def readOnly(v: Boolean): this.type = mutate {
    _readOnly = () => v
  }

  def readOnly(f: () => Boolean): this.type = mutate {
    _readOnly = f
  }

  var currentlyReadOnly: Boolean = false

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    currentlyReadOnly = readOnly
    if (currentlyReadOnly) input.withAttr("readonly", "true") else input
  }

  def updateFieldReadOnlyStatus()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = _readOnly().pipe(shouldBeReadOnly => {
    if (shouldBeReadOnly != currentlyReadOnly) {
      currentlyReadOnly = shouldBeReadOnly
      if (currentlyReadOnly) {
        JS.setAttr(elemId)("readonly", "true")
      } else {
        JS.removeAttr(elemId, "readonly")
      }
    } else {
      JS.void
    }
  })

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ & updateFieldReadOnlyStatus())
}
