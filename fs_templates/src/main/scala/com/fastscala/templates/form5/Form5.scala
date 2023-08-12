package com.fastscala.templates.form5

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form5.fields._
import com.fastscala.templates.utils.ElemWithRandomId
import com.fastscala.utils.RenderableWithFSContext

import scala.xml.{Elem, NodeSeq}

trait FormRenderer {

  def render(form: Elem): NodeSeq
}

trait Form5 extends RenderableWithFSContext with ElemWithRandomId {

  implicit def form = this

  val rootField: FormField

  def initForm()(implicit fsc: FSContext): Unit = ()

  def formRenderHits(): Seq[RenderHint] = Nil

  def onEvent(event: FormEvent)(implicit form: Form5, fsc: FSContext): Js = {
    implicit val renderHints = formRenderHits()
    rootField.onEvent(event)
  }

  def renderer: FormRenderer

  def reRender()(implicit fsc: FSContext): Js = {
    implicit val renderHints = formRenderHits()
    rootField.reRender()
  }

  def render()(implicit fsc: FSContext): Elem = {
    implicit val renderHints = formRenderHits()
    rootField.render()
  }

  def beforeSave()(implicit fsc: FSContext): Js = Js.void

  def afterSave()(implicit fsc: FSContext): Js = Js.void

  def onSaveServerSide()(implicit fsc: FSContext): Js = {
    val hasErrors = rootField.enabledFields.exists({ case field: ValidatableField => field.hasErrors_?() case _ => false })
    implicit val renderHints = formRenderHits() :+ OnSaveRerender
    if (hasErrors) {
      rootField.reRender()(this, fsc, renderHints :+ ShowValidationsHint)
    } else {
      beforeSave() &
        rootField.onEvent(BeforeSave) &
        rootField.onEvent(PerformSave) &
        rootField.onEvent(AfterSave) &
        afterSave() &
        rootField.reRender() &
        Js.consoleLog("HERE!!")
    }
  }

  def onSaveClientSide()(implicit fsc: FSContext): Js =
    fsc.callback(() => onSaveServerSide())
}
