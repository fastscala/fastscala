package com.fastscala.templates.form7

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7.mixins.{F7FieldWithDependencies, F7FieldWithEnabled, F7FieldWithState}
import com.fastscala.templates.utils.ElemWithRandomId
import com.fastscala.xml.scala_xml.JS

import scala.xml.{Elem, NodeSeq}

/**
 * A field can contain other fields.
 */
trait F7Field
  extends F7FieldWithDependencies
    with F7FieldWithState
    with F7FieldWithEnabled
    with ElemWithRandomId {

  def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = event match {
    case ChangedField(field) if deps.contains(field) => updateFieldStatus() & form.onEvent(ChangedField(this))
    case ChangedField(f) if f == this => updateFieldStatus()
    case _ => Js.void
  }

  def updateFieldStatus()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = Js.void

  val aroundId: String = randomElemId

  def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem

  def postRenderSetupJs()(implicit fsc: FSContext): Js = Js.void

  def preValidation()(implicit form: Form7, fsc: FSContext): Js = Js.void

  def validate(): Seq[(F7Field, NodeSeq)] = Nil

  def postValidation(errors: Seq[(F7Field, NodeSeq)])(implicit form: Form7, fsc: FSContext): Js = Js.void

  def preSubmit()(implicit form: Form7, fsc: FSContext): Js = Js.void

  def submit()(implicit form: Form7, fsc: FSContext): Js = Js.void

  def postSubmit()(implicit form: Form7, fsc: FSContext): Js = Js.void

  def fieldAndChildreenMatchingPredicate(predicate: PartialFunction[F7Field, Boolean]): List[F7Field]

  def deps: Set[F7Field]

  def enabled: Boolean

  def disabled: Boolean

  def reRender()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = {
    JS.replace(aroundId, render()) & postRenderSetupJs()
  }

  def withFieldRenderHints[T](f: Seq[RenderHint] => T)(implicit renderHints: Seq[RenderHint]): T = f {
    List(DisableFieldsHint).filter(_ => disabled) ++
      renderHints
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
}
