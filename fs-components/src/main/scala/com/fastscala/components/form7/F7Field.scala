package com.fastscala.components.form7

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.form7.mixins.{F7FieldWithDependencies, F7FieldWithEnabled, F7FieldWithId, F7FieldWithOnChangedField, F7FieldWithState}
import com.fastscala.components.utils.{ElemWithId, ElemWithRandomId}
import com.fastscala.utils.IdGen

import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, NodeSeq}

/**
 * A field can contain other fields.
 */
trait F7Field
  extends F7FieldWithDependencies
    with F7FieldWithState
    with F7FieldWithEnabled
    with ElemWithId
    with F7FieldWithId {

  def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext): Js = event match {
    case ChangedField(field) if deps.contains(field) => updateFieldWithoutReRendering().getOrElse(reRender()) &
      // If this field (B) depends on field A and field C depends on B, then we want to trigger also a ChangedField event for this field (B), so that field C also updates:
      form.onEvent(ChangedField(this))
    case _ => JS.void
  }

  /**
   * Tries to update the field without needing to rerender.
   */
  def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): Try[Js] = Success(JS.void)
  
  def updateFieldWithoutReRenderingOrFallbackToRerender()(implicit form: Form7, fsc: FSContext): Js = updateFieldWithoutReRendering().getOrElse(reRender())

  val aroundId: String = IdGen.id

  def render()(implicit form: Form7, fsc: FSContext): Elem

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

  def reRender()(implicit form: Form7, fsc: FSContext): Js = {
    JS.replace(aroundId, render()) & postRenderSetupJs()
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
        case ValidateOnAttemptSubmitOnly => form.state() match {
          case Initial => false
          case Modified => false
          case ValidationFailed => true
          case Saved => false
        }
      }
    }

    aux(form.validationStrategy)
  }
}
