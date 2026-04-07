package com.fastscala.components.form7.mixins.mainelem

import com.fastscala.components.form7.mixins.F7FieldFocusable
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS


trait F7FieldFocusableMainElem extends F7FieldFocusable with F7FieldWithMainElem {

  override def focusJs: Js = JS.focus(mainElemId)
}
