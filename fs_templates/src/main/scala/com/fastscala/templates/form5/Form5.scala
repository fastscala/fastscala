package com.fastscala.templates.form5

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.templates.form5.fields._
import com.fastscala.templates.utils.ElemWithRandomId

trait FormRenderer[E <: FSXmlEnv] {

  def render(form: E#Elem): E#NodeSeq
}

abstract class DefaultForm5[E <: FSXmlEnv]()(implicit val formRenderer: FormRenderer[E]) extends Form5[E]

trait Form5[E <: FSXmlEnv] extends ElemWithRandomId {

  implicit def fsXmlSupport: FSXmlSupport[E]

  implicit def form = this

  val rootField: FormField[E]

  def initForm()(implicit fsc: FSContext): Unit = ()

  def formRenderHits(): Seq[RenderHint] = Nil

  def onEvent(event: FormEvent)(implicit form: Form5[E], fsc: FSContext): Js = {
    implicit val renderHints = formRenderHits()
    rootField.onEvent(event)
  }

  def formRenderer: FormRenderer[E]

  def focusFirstFocusableFieldJs(): Js =
    rootField.fieldsMatching({ case _: FocusableFormField[E] => true })
      .collectFirst({ case fff: FocusableFormField[E] => fff })
      .map(_.focusJs)
      .getOrElse(Js.void)

  def afterRendering()(implicit fsc: FSContext): Js = Js.void

  def reRender()(implicit fsc: FSContext): Js = {
    implicit val renderHints = formRenderHits()
    rootField.reRender() & afterRendering()
  }

  def render()(implicit fsc: FSContext): E#Elem = {
    import com.fastscala.core.FSXmlUtils._
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
      val hasErrors = rootField.enabledFields.exists({ case field: ValidatableField[E] => field.hasErrors_?() case _ => false })
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
