package com.fastscala.components.form7.mixins

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.form7.*
import com.fastscala.utils.IdGen

import scala.xml.NodeSeq

trait F7FieldWithValidation extends F7Field
  with F7FieldWithOnChangedField
  with F7FieldWithValidationRules {

  def labelId: String

  def invalidFeedbackId: String = "invalid_feedback_" + IdGen.id

  def validFeedbackId: String

  def helpId: String

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ & updateValidation())

  override def postValidation(errors: Seq[(F7Field, NodeSeq)])(implicit form: Form7, fsc: FSContext): Js = {
    updateValidation()
  }

  def showOrUpdateValidation(ns: NodeSeq): Js

  def hideValidation(): Js

  var showingValidation = false

  def updateValidation()(implicit form7: Form7): Js = {
    val shouldShowValidation = shouldShowValidation_?
    if (shouldShowValidation) {
      val errors = this.validate()
      if (errors.nonEmpty) {
        val validation = errors.headOption.map(error => <div>{error._2}</div>).getOrElse(<div></div>)
        showingValidation = true
        showOrUpdateValidation(validation)
      } else {
        showingValidation = false
        hideValidation()
      }
    } else if (showingValidation) {
      showingValidation = false
      hideValidation()
    } else JS.void
  }
}
