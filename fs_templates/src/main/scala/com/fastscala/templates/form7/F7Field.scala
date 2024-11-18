package com.fastscala.templates.form7

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS
import com.fastscala.templates.form7.mixins.{F7FieldWithDependencies, F7FieldWithEnabled, F7FieldWithState}
import com.fastscala.templates.utils.ElemWithRandomId

import scala.util.{Failure, Success, Try}
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
    case ChangedField(field) if deps.contains(field) => updateFieldWithoutReRendering().getOrElse(reRender()) &
      // If this field (B) depends on field A and field C depends on B, then we want to trigger also a ChangedField event for this field (B), so that field C also updates:
      form.onEvent(ChangedField(this))
    case _ => JS.void
  }

  /**
   * Tries to update the field without needing to rerender. If
   */
  def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Try[Js] = Success(JS.void)

  val aroundId: String = randomElemId

  def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem

  def postRenderSetupJs()(implicit fsc: FSContext): Js = JS.void

  def preValidation()(implicit form: Form7, fsc: FSContext): Js = JS.void

  def validate(): Seq[(F7Field, NodeSeq)] = Nil

  def postValidation(errors: Seq[(F7Field, NodeSeq)])(implicit form: Form7, fsc: FSContext): Js = JS.void

  def preSubmit()(implicit form: Form7, fsc: FSContext): Js = JS.void

  def submit()(implicit form: Form7, fsc: FSContext): Js = JS.void

  def postSubmit()(implicit form: Form7, fsc: FSContext): Js = JS.void

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
    import F7FormValidationStrategy.*
    import Form7State.*
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
