package com.fastscala.templates.form7.fields.text

import com.fastscala.templates.form7.mixins._

import scala.xml.Elem


trait F7FieldInputFieldMixin extends F7FieldWithValidations {

  def processInputElem(input: Elem): Elem = input
}
