package com.fastscala.components.form7

import com.fastscala.components.form7.formmixins.F7FormWithValidationStrategy
import com.fastscala.components.form7.mixins.FocusableF7Field
import com.fastscala.components.utils.ElemWithRandomId
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.utils.RenderableWithFSContext

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

trait F7FormRenderer {

  def render(form: Elem): NodeSeq
}

abstract class DefaultForm7()(implicit val formRenderer: F7FormRenderer) extends Form7

object Form7State extends Enumeration {
  val Filling = Value
  val ValidationFailed = Value
  val Saved = Value
}

trait Form7 extends RenderableWithFSContext with ElemWithRandomId with F7FormWithValidationStrategy {

  implicit def form: this.type = this

  var state = Form7State.Filling

  def onChangedState(from: Form7State.Value, to: Form7State.Value): Js = Js.Void

  /** NOTE: Implementation should usually be a val or lazy val! don't re-instantiate the fields every time this method is called!
    */
  def rootField: F7Field

  def initForm()(implicit fsc: FSContext): Unit = ()

  def formRenderHits(): Seq[RenderHint] = Nil

  def changedField(field: F7Field)(implicit fsc: FSContext): Js = onEvent(ChangedField(field))(this, fsc)

  def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext): Js = {
    implicit val renderHints = formRenderHits()
    (event match {
      case RequestedSubmit(_) => submitFormServerSide()
      case ChangedField(_) =>
        val previousState = state
        state = Form7State.Filling
        onChangedState(previousState, state)
      case _ => Js.Void
    }) &
      rootField.onEvent(event)
  }

  def formRenderer: F7FormRenderer

  def focusFirstFocusableFieldJs(): Js =
    rootField.fieldAndChildreenMatchingPredicate({ case _: FocusableF7Field => true })
      .collectFirst({ case fff: FocusableF7Field => fff })
      .map(_.focusJs)
      .getOrElse(JS.void)

  def render()(implicit fsc: FSContext): Elem = {
    implicit val renderHints = formRenderHits()
    val rendered = rootField.render()
    if (postRenderSetupJs() != JS.void) {
      rendered.withAppendedToContents(JS.inScriptTag(postRenderSetupJs().onDOMContentLoaded))
    } else {
      rendered
    }
  }

  /** Used to run JS to initialize the form after it is rendered or re-rendered.
    */
  def postRenderSetupJs()(implicit fsc: FSContext): Js = rootField.fieldAndChildreenMatchingPredicate(_.enabled).map(_.postRenderSetupJs()).reduceOption(_ & _).getOrElse(JS.void)

  def reRender()(implicit fsc: FSContext): Js = {
    implicit val renderHints = formRenderHits()
    rootField.reRender() & postRenderSetupJs()
  }

  def preValidateForm()(implicit fsc: FSContext): Js = JS.void

  def postValidateForm(errors: List[(F7Field, NodeSeq)])(implicit fsc: FSContext): Js = JS.void

  def preSubmitForm()(implicit fsc: FSContext): Js = JS.void

  def postSubmitForm()(implicit fsc: FSContext): Js = JS.void

  def submitFormClientSide()(implicit fsc: FSContext): Js = fsc.page.rootFSContext.callback(() => submitFormServerSide())

  def submitFormServerSide()(implicit fsc: FSContext): Js = {
    if (fsc != fsc.page.rootFSContext) submitFormServerSide()(fsc.page.rootFSContext)
    else {
      val enabledFields = rootField.fieldAndChildreenMatchingPredicate(_.enabled)
      preValidateForm() &
        enabledFields.map(_.preValidation()).reduceOption(_ & _).getOrElse(JS.void) &
        enabledFields.collect(_.validate()).flatten.pipe(errors => {
          (if (errors.nonEmpty) {
             val previousState = state
             state = Form7State.ValidationFailed
             onChangedState(previousState, state)
           } else Js.Void)
          &
            enabledFields.map(_.postValidation(errors)).reduceOption(_ & _).getOrElse(JS.void) &
            postValidateForm(errors) &
            (if (errors.isEmpty) {
               savePipeline(enabledFields) & {
                 val previousState = state
                 state = Form7State.Saved
                 onChangedState(previousState, state)
               }
             } else JS.void)
        })
    }
  }

  private def savePipeline(enabledFields: List[F7Field])(implicit fsc: FSContext): Js = {
    preSubmitForm() &
      enabledFields.map(_.preSubmit()).reduceOption(_ & _).getOrElse(JS.void) &
      enabledFields.map(_.submit()).reduceOption(_ & _).getOrElse(JS.void) &
      enabledFields.map(_.postSubmit()).reduceOption(_ & _).getOrElse(JS.void) &
      postSubmitForm() // &      rootField.reRender()
  }
}
