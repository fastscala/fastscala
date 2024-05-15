package com.fastscala.templates.form6

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form6.fields._
import com.fastscala.templates.utils.ElemWithRandomId
import com.fastscala.utils.ElemTransformers.RichElem
import com.fastscala.utils.RenderableWithFSContext

import scala.xml.{Elem, NodeSeq}

trait FormRenderer {

  def render(form: Elem): NodeSeq
}

abstract class DefaultForm6()(implicit val formRenderer: FormRenderer) extends Form6

trait Form6 extends RenderableWithFSContext with ElemWithRandomId {

  implicit def form = this

  val rootField: FormField

  def initForm()(implicit fsc: FSContext): Unit = ()

  def formRenderHits(): Seq[RenderHint] = Nil

  def onEvent(event: FormEvent)(implicit form: Form6, fsc: FSContext): Js = {
    implicit val renderHints = formRenderHits()
    rootField.onEvent(event)
  }

  def formRenderer: FormRenderer

  def focusFirstFocusableFieldJs(): Js =
    rootField.fieldsMatching({ case _: FocusableFormField => true })
      .collectFirst({ case fff: FocusableFormField => fff })
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
      rendered.withAppendedToContents(afterRendering().onDOMContentLoaded.inScriptTag)
    } else {
      rendered
    }
  }

  def beforeSave()(implicit fsc: FSContext): Js = Js.void

  def afterSave()(implicit fsc: FSContext): Js = Js.void

  def onSaveServerSide()(implicit fsc: FSContext): Js = {
    if (fsc != fsc.page.rootFSContext) onSaveServerSide()(fsc.page.rootFSContext)
    else {
      val hasErrors = rootField.enabledFields.exists({ case field: ValidatableField => field.hasErrors_?() case _ => false })
      implicit val renderHints = formRenderHits() :+ OnSaveRerender
      if (hasErrors) {
        rootField.onEvent(ErrorsOnSave) &
          rootField.reRender()(this, fsc, renderHints :+ ShowValidationsHint :+ FailedSaveStateHint)
      } else {
        beforeSave() &
          rootField.onEvent(BeforeSave) &
          rootField.onEvent(PerformSave) &
          rootField.onEvent(AfterSave) &
          afterSave() &
          rootField.reRender()
      }
    }
  }

  def onSaveClientSide()(implicit fsc: FSContext): Js = fsc.page.rootFSContext.callback(() => onSaveServerSide())
}
