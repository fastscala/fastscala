package com.fastscala.templates.form7.fields.text

import com.fastscala.templates.form7.F7Field

import scala.xml.Elem


trait F7FieldInputFieldMixin extends F7Field {

  def processInputElem(input: Elem): Elem = input
}
