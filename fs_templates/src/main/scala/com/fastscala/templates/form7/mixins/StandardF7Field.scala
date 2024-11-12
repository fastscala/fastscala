package com.fastscala.templates.form7.mixins

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7._
import com.fastscala.templates.form7.renderers.StandardF7FieldRenderer

import scala.xml.NodeSeq

trait StandardF7Field extends F7Field
  with F7FieldWithValidations {

  def renderer: StandardF7FieldRenderer

  def visible: () => Boolean = () => enabled

  override def updateFieldStatus()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = super.updateFieldStatus() &
    updateValidation()

  override def postValidation(errors: Seq[(F7Field, NodeSeq)])(implicit form: Form7, fsc: FSContext): Js = {
    implicit val renderHints: Seq[RenderHint] = form.formRenderHits()
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
    } else Js.void
  }

  override def postSubmit()(implicit form: Form7, fsc: FSContext): Js = super.postSubmit() & {
    setFilled()
    Js.void
  }

  override def fieldAndChildreenMatchingPredicate(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] = if (predicate.applyOrElse[F7Field, Boolean](this, _ => false)) List(this) else Nil
}
