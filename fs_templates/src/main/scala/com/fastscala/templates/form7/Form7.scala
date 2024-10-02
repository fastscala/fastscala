package com.fastscala.templates.form7

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7.fields._
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
    rootField.fieldsMatching({ case _: FocusableF7Field => true })
      .collectFirst({ case fff: FocusableF7Field => fff })
      .map(_.focusJs)
      .getOrElse(Js.void)

  def afterRendering()(implicit fsc: FSContext): Js = Js.void

  def reRender()(implicit fsc: FSContext): Js = {
    implicit val renderHints = formRenderHits()
    rootField.reRender() & afterRendering()
  }

  def render()(implicit fsc: FSContext): Elem = {
    implicit val renderHints = formRenderHits()
    val rendered = rootField.render()
    if (afterRendering() != Js.void) {
      rendered.withAppendedToContents(JS.inScriptTag(afterRendering().onDOMContentLoaded))
    } else {
      rendered
    }
  }

  def preSave()(implicit fsc: FSContext): Js = Js.void

  def postSubmit()(implicit fsc: FSContext): Js = Js.void

  def onSaveServerSide()(implicit fsc: FSContext): Js = {
    if (fsc != fsc.page.rootFSContext) onSaveServerSide()(fsc.page.rootFSContext)
    else {
      val errors: List[(ValidatableF7Field, NodeSeq)] = rootField.enabledFields.collect({ case field: ValidatableF7Field => field.errors() }).flatten
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
    preSave() &
      rootField.onEvent(PreSubmit) &
      rootField.onEvent(Submit) &
      rootField.onEvent(PostSubmit) &
      postSubmit() &
      rootField.reRender()
  }

  def onSaveClientSide()(implicit fsc: FSContext): Js = fsc.page.rootFSContext.callback(() => onSaveServerSide())
}
