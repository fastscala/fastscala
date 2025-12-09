package com.fastscala.components.bootstrap5.form7.fields

import com.fastscala.components.form7.fields.F7HtmlField

import scala.xml.Elem

class F7LabelField(elem: Elem) extends F7HtmlField(_ => elem) {
  def this(contents: String) = this(<label>{contents}</label>)
}
