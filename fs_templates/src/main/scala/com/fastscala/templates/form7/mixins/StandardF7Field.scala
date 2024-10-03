package com.fastscala.templates.form7.mixins

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7._
import com.fastscala.templates.utils.ElemWithRandomId
import com.fastscala.xml.scala_xml.JS


abstract class StandardF7Field() extends F7Field with ElemWithRandomId {

  val aroundId: String = randomElemId

  def reRender()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = {
    JS.replace(aroundId, render())
  }

  def visible: () => Boolean = () => enabled()

  override def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case ChangedField(field) if deps.contains(field) => reRender() & form.onEvent(ChangedField(this))
    case _ => Js.void
  })

  def disabled(): Boolean

  def readOnly(): Boolean

  def withFieldRenderHints[T](f: Seq[RenderHint] => T)(implicit renderHints: Seq[RenderHint]): T = f {
    List(DisableFieldsHint).filter(_ => disabled()) ++
      List(ReadOnlyFieldsHint).filter(_ => readOnly()) ++
      renderHints
  }
}
