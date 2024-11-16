package com.fastscala.templates.form7.mixins

import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem


trait FocusableF7Field {

  def focusJs: Js
}
