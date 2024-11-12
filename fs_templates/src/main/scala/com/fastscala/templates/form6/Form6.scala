package com.fastscala.templates.form6

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form6.fields._
import com.fastscala.templates.utils.ElemWithRandomId
import com.fastscala.utils.RenderableWithFSContext
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.xml.scala_xml.{FSScalaXmlEnv, JS}

import scala.xml.{Elem, NodeSeq}

trait F6FormRenderer {

  def render(form: Elem): NodeSeq
}

abstract class DefaultForm6()(implicit val formRenderer: F6FormRenderer) extends Form6

trait Form6 extends RenderableWithFSContext[FSScalaXmlEnv.type] with ElemWithRandomId {

  implicit def form: Form6 = this

  val rootField: F6Field

  def initForm()(implicit fsc: FSContext): Unit = ()

  def formRenderHits(): Seq[RenderHint] = Nil

  def onEvent(event: FormEvent)(implicit form: Form6, fsc: FSContext): Js = {
    implicit val renderHints = formRenderHits()
    event match {
      case RequestedSubmit(_) => savePipeline()
      case event => rootField.onEvent(event)
    }
  }

  def formRenderer: F6FormRenderer

  def focusFirstFocusableFieldJs(): Js =
    rootField.fieldsMatching({ case _: FocusableF6Field => true })
      .collectFirst({ case fff: FocusableF6Field => fff })
      .map(_.focusJs)
      .getOrElse(Js.void)

  def afterRendering()(implicit fsc: FSContext): Js = Js.void

  def reRender()(implicit fsc: FSContext): Js = {
    implicit val renderHints = formRenderHits()
    rootField.reRender() & afterRendering()
  }

  def render()(implicit fsc: FSContext): FSScalaXmlEnv.Elem = {
    implicit val renderHints = formRenderHits()
    val rendered = rootField.render()
    if (afterRendering() != Js.void) {
      rendered.withAppendedToContents(JS.inScriptTag(afterRendering().onDOMContentLoaded))
    } else {
      rendered
    }
  }

  def preSave()(implicit fsc: FSContext): Js = Js.void

  def postSave()(implicit fsc: FSContext): Js = Js.void

  def onSaveServerSide()(implicit fsc: FSContext): Js = {
    if (fsc != fsc.page.rootFSContext) onSaveServerSide()(fsc.page.rootFSContext)
    else {
      val hasErrors = rootField.enabledFields.exists({ case field: ValidatableF6Field => field.hasErrors_?() case _ => false })
      implicit val renderHints: Seq[RenderHint] = formRenderHits() :+ OnSaveRerender
      if (hasErrors) {
        rootField.onEvent(FailedSave) &
          rootField.reRender()(this, fsc, renderHints :+ ShowValidationsHint :+ FailedSaveStateHint)
      } else {
        savePipeline()
      }
    }
  }

  private def savePipeline()(implicit renderHints: Seq[RenderHint], fsc: FSContext): Js = {
    preSave() &
      rootField.onEvent(PreSave) &
      rootField.onEvent(Save) &
      rootField.onEvent(PostSave) &
      postSave() &
      rootField.reRender()
  }

  def onSaveClientSide()(implicit fsc: FSContext): Js = fsc.page.rootFSContext.callback(() => onSaveServerSide())
}
