package com.fastscala.templates.form7.mixins

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7._
import com.fastscala.templates.form7.renderers.StandardF7FieldRenderer

import scala.xml.NodeSeq


abstract class StandardF7Field() extends F7Field with F7FieldWithValidations {

  def renderer: StandardF7FieldRenderer

  var showingValidation = false

  def visible: () => Boolean = () => enabled()

  override def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case ChangedField(field) if deps.contains(field) => reRender() & form.onEvent(ChangedField(this))
    case ChangedField(f) if f == this => updateValidation()
    case _ => Js.void
  })

  override def postValidation(errors: Seq[(F7Field, NodeSeq)])(implicit form: Form7, fsc: FSContext): Js = {
    implicit val renderHints: Seq[RenderHint] = form.formRenderHits()
    updateValidation()
  }

  def shouldShowValidation_?(implicit form: Form7): Boolean = {
    import F7FormValidationStrategy._
    import Form7State._
    def aux(validationStrategy: F7FormValidationStrategy.Value): Boolean = {
      validationStrategy match {
        case ValidateBeforeUserInput => true
        case ValidateEachFieldAfterUserInput => state match {
          case F7FieldState.Filled => true
          case F7FieldState.AwaitingInput => aux(ValidateOnAttemptSubmitOnly)
        }
        case ValidateOnAttemptSubmitOnly => form.state match {
          case Filling => false
          case ValidationFailed => true
          case Saved => false
        }
      }
    }

    aux(form.validationStrategy)
  }

  def showOrUpdateValidation(ns: NodeSeq): Js

  def hideValidation(): Js

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
