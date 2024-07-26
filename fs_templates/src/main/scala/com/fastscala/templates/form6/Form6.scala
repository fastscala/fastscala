package com.fastscala.templates.form6

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.templates.form6.fields._
import com.fastscala.templates.utils.ElemWithRandomId
import com.fastscala.utils.RenderableWithFSContext

trait F6FormRenderer[E <: FSXmlEnv] {

  def render(form: E#Elem): E#NodeSeq
}

abstract class DefaultForm6[E <: FSXmlEnv]()(implicit val formRenderer: F6FormRenderer[E]) extends Form6[E]

trait Form6[E <: FSXmlEnv] extends RenderableWithFSContext with ElemWithRandomId {

  implicit def fsXmlSupport: FSXmlSupport[E]

  import com.fastscala.core.FSXmlUtils._

  implicit def form = this

  val rootField: F6Field[E]

  def initForm()(implicit fsc: FSContext): Unit = ()

  def formRenderHits(): Seq[RenderHint] = Nil

  def onEvent(event: FormEvent)(implicit form: Form6[E], fsc: FSContext): Js = {
    implicit val renderHints = formRenderHits()
    event match {
      case RequestedSubmit(_) => savePipeline()
      case event => rootField.onEvent(event)
    }
  }

  def formRenderer: F6FormRenderer[E]

  def focusFirstFocusableFieldJs(): Js =
    rootField.fieldsMatching({ case _: FocusableF6Field[E] => true })
      .collectFirst({ case fff: FocusableF6Field[E] => fff })
      .map(_.focusJs)
      .getOrElse(Js.void)

  def afterRendering()(implicit fsc: FSContext): Js = Js.void

  def reRender()(implicit fsc: FSContext): Js = {
    implicit val renderHints = formRenderHits()
    rootField.reRender() & afterRendering()
  }

  def render()(implicit fsc: FSContext): E#Elem = {
    implicit val renderHints = formRenderHits()
    val rendered = rootField.render()
    if (afterRendering() != Js.void) {
      rendered.withAppendedToContents(afterRendering().onDOMContentLoaded.inScriptTag)
    } else {
      rendered
    }
  }

  def preSave()(implicit fsc: FSContext): Js = Js.void

  def postSave()(implicit fsc: FSContext): Js = Js.void

  def onSaveServerSide()(implicit fsc: FSContext): Js = {
    if (fsc != fsc.page.rootFSContext) onSaveServerSide()(fsc.page.rootFSContext)
    else {
      val hasErrors = rootField.enabledFields.exists({ case field: ValidatableField[E] => field.hasErrors_?() case _ => false })
      implicit val renderHints: Seq[RenderHint] = formRenderHits() :+ OnSaveRerender
      if (hasErrors) {
        rootField.onEvent(ErrorsOnSave) &
          rootField.reRender()(this, fsc, renderHints :+ ShowValidationsHint :+ FailedSaveStateHint)
      } else {
        savePipeline()
      }
    }
  }

  private def savePipeline()(implicit renderHints: Seq[RenderHint], fsc: FSContext): Js = {
    preSave() &
      rootField.onEvent(BeforeSave) &
      rootField.onEvent(PerformSave) &
      rootField.onEvent(AfterSave) &
      postSave() &
      rootField.reRender()
  }

  def onSaveClientSide()(implicit fsc: FSContext): Js = fsc.page.rootFSContext.callback(() => onSaveServerSide())
}
