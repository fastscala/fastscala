package com.fastscala.templates.form4

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.templates.utils.ElemWithRandomId

trait Form[E <: FSXmlEnv] extends ElemWithRandomId {

  implicit def fsXmlSupport: FSXmlSupport[E]

  implicit def form: Form[E] = this

  val rootField: FormField[E]

  def reRender()(implicit fsc: FSContext): Js = rootField.reRender()

  def render()(implicit fsc: FSContext): E#NodeSeq = rootField.render()
}
