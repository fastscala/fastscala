package com.fastscala.templates.form4

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.utils.ElemWithRandomId
import com.fastscala.utils.RenderableWithFSContext

import scala.xml.NodeSeq

trait Form extends RenderableWithFSContext with ElemWithRandomId {

  implicit def form = this

  val rootField: FormField

  def reRender()(implicit fsc: FSContext): Js = rootField.reRender()

  def render()(implicit fsc: FSContext): NodeSeq = rootField.render()
}
