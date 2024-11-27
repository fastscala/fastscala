package com.fastscala.components.form7.mixins

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.form7.{F7Field, Form7, RenderHint}
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem


trait F7OnChangedFieldHandler {
  def onChanged(field: F7Field)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js
}
