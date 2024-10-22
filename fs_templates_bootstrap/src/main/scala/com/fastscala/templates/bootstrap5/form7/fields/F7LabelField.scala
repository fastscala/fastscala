package com.fastscala.templates.bootstrap5.form7.fields

import com.fastscala.templates.form7.fields.F7HtmlField

import scala.xml.Elem

class F7LabelField(elem: Elem) extends F7HtmlField(elem) {
  def this(contents: String) = this(<label>{contents}</label>)
}
