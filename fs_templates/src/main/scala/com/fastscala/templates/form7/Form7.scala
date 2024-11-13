package com.fastscala.templates.form7

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7.formmixins.F7FormWithValidationStrategy
import com.fastscala.templates.form7.mixins.FocusableF7Field
import com.fastscala.templates.utils.ElemWithRandomId
import com.fastscala.utils.RenderableWithFSContext
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.xml.scala_xml.{FSScalaXmlEnv, JS}

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

trait Form7 extends RenderableWithFSContext[FSScalaXmlEnv.type] with ElemWithRandomId
  with F7FormWithValidationStrategy {

  implicit def form: this.type = this

  var state = Form7State.Filling

  /**
   * NOTE: Implementation should usually be a val or lazy val! don't re-instantiate the fields every time this method is called!
   * */
  def rootField: F7Field

  def initForm()(implicit fsc: FSContext): Unit = ()

  def formRenderHits(): Seq[RenderHint] = Nil

  def changedField(field: F7Field)(implicit fsc: FSContext): Js = onEvent(ChangedField(field))(this, fsc)

  def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext): Js = {
    implicit val renderHints = formRenderHits()
    event match {
      case RequestedSubmit(_) => submitFormServerSide()
      case ChangedField(_) =>
        state = Form7State.Filling
      case _ =>
    }
    rootField.onEvent(event)
  }

  def formRenderer: F7FormRenderer

  def focusFirstFocusableFieldJs(): Js =
    rootField.fieldAndChildreenMatchingPredicate({ case _: FocusableF7Field => true })
      .collectFirst({ case fff: FocusableF7Field => fff })
      .map(_.focusJs)
      .getOrElse(Js.void)

  def render()(implicit fsc: FSContext): Elem = {
    implicit val renderHints = formRenderHits()
    val rendered = rootField.render()
    if (postRenderSetupJs() != Js.void) {
      rendered.withAppendedToContents(JS.inScriptTag(postRenderSetupJs().onDOMContentLoaded))
    } else {
      rendered
    }
  }

  /**
   * Used to run JS to initialize the form after it is rendered or re-rendered.
   */
  def postRenderSetupJs()(implicit fsc: FSContext): Js = rootField.fieldAndChildreenMatchingPredicate(_.enabled).map(_.postRenderSetupJs()).reduceOption(_ & _).getOrElse(Js.void)

  def reRender()(implicit fsc: FSContext): Js = {
    implicit val renderHints = formRenderHits()
    rootField.reRender() & postRenderSetupJs()
  }

  def preValidateForm()(implicit fsc: FSContext): Js = Js.void

  def postValidateForm(errors: List[(F7Field, NodeSeq)])(implicit fsc: FSContext): Js = Js.void

  def preSubmitForm()(implicit fsc: FSContext): Js = Js.void

  def postSubmitForm()(implicit fsc: FSContext): Js = Js.void

  def submitFormClientSide()(implicit fsc: FSContext): Js = fsc.page.rootFSContext.callback(() => submitFormServerSide())

  def submitFormServerSide()(implicit fsc: FSContext): Js = {
    if (fsc != fsc.page.rootFSContext) submitFormServerSide()(fsc.page.rootFSContext)
    else {
      val enabledFields = rootField.fieldAndChildreenMatchingPredicate(_.enabled)
      preValidateForm() &
        enabledFields.map(_.preValidation()).reduceOption(_ & _).getOrElse(Js.void) &
        enabledFields.collect(_.validate()).flatten.pipe(errors => {
          if (errors.nonEmpty) {
            state = Form7State.ValidationFailed
          }
          enabledFields.map(_.postValidation(errors)).reduceOption(_ & _).getOrElse(Js.void) &
            postValidateForm(errors) &
            (if (errors.isEmpty) {
              try {
                savePipeline(enabledFields)
              } finally {
                state = Form7State.Saved
              }
            } else Js.void)
        })
    }
  }

  private def savePipeline(enabledFields: List[F7Field])(implicit fsc: FSContext): Js = {
    preSubmitForm() &
      enabledFields.map(_.preSubmit()).reduceOption(_ & _).getOrElse(Js.void) &
      enabledFields.map(_.submit()).reduceOption(_ & _).getOrElse(Js.void) &
      enabledFields.map(_.postSubmit()).reduceOption(_ & _).getOrElse(Js.void) &
      postSubmitForm() //&      rootField.reRender()
  }
}
