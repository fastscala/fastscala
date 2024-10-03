package com.fastscala.templates.form7

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7.mixins.FocusableF7Field
import com.fastscala.templates.utils.ElemWithRandomId
import com.fastscala.utils.RenderableWithFSContext
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.xml.scala_xml.{FSScalaXmlEnv, JS}

import scala.xml.{Elem, NodeSeq}

trait F7FormRenderer {

  def render(form: Elem): NodeSeq
}

abstract class DefaultForm7()(implicit val formRenderer: F7FormRenderer) extends Form7

trait Form7 extends RenderableWithFSContext[FSScalaXmlEnv.type] with ElemWithRandomId {

  implicit def form: this.type = this

  val rootField: F7Field

  def initForm()(implicit fsc: FSContext): Unit = ()

  def formRenderHits(): Seq[RenderHint] = Nil

  def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext): Js = {
    implicit val renderHints = formRenderHits()
    event match {
      case RequestedSubmit(_) => savePipeline()
      case event => rootField.onEvent(event)
    }
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
  def postRenderSetupJs()(implicit fsc: FSContext): Js = rootField.fieldAndChildreenMatchingPredicate(_.enabled()).map(_.postRenderSetupJs()).reduceOption(_ & _).getOrElse(Js.void)

  def reRender()(implicit fsc: FSContext): Js = {
    implicit val renderHints = formRenderHits()
    rootField.reRender() & postRenderSetupJs()
  }

  def preSubmitForm()(implicit fsc: FSContext): Js = Js.void

  def postSubmitForm()(implicit fsc: FSContext): Js = Js.void

  def submitFormClientSide()(implicit fsc: FSContext): Js = fsc.page.rootFSContext.callback(() => submitFormServerSide())

  def submitFormServerSide()(implicit fsc: FSContext): Js = {
    if (fsc != fsc.page.rootFSContext) submitFormServerSide()(fsc.page.rootFSContext)
    else {
      val errors: List[(F7Field, NodeSeq)] =
        rootField
          .fieldAndChildreenMatchingPredicate(_.enabled())
          .collect(_.validate())
          .flatten
      implicit val renderHints: Seq[RenderHint] = formRenderHits() :+ OnSaveRerender
      if (errors.nonEmpty) {
        rootField.onEvent(PostValidation(errors)) &
          rootField.reRender()(this, fsc, renderHints :+ ShowValidationsHint :+ FailedSaveStateHint)
      } else {
        savePipeline()
      }
    }
  }

  private def savePipeline()(implicit renderHints: Seq[RenderHint], fsc: FSContext): Js = {
    val enabledFields = rootField.fieldAndChildreenMatchingPredicate(_.enabled())
    preSubmitForm() &
      enabledFields.map(_.preSubmit()).reduceOption(_ & _).getOrElse(Js.void) &
      enabledFields.map(_.submit()).reduceOption(_ & _).getOrElse(Js.void) &
      enabledFields.map(_.postSubmit()).reduceOption(_ & _).getOrElse(Js.void) &
      postSubmitForm() &
      rootField.reRender()
  }
}
