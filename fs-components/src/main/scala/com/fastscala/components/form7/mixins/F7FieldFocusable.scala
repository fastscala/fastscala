package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7Field, F7FieldMixinStatus}
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.xml.ScalaXmlElemUtils.RichElem


trait F7FieldFocusable extends F7Field {

  private val _focusOnRender: F7FieldMixinStatus[Boolean] = F7FieldMixinStatus(false)

  def focusOnRender: Boolean = _focusOnRender()

  def focusOnRender(v: Boolean): this.type = mutate {
    _focusOnRender() = () => v
  }

  def focusJs: Js

  override def postRenderSetupJs()(implicit fsc: FSContext): Js = super.postRenderSetupJs() &
    JS.evalIf(focusOnRender)(focusJs)
}
