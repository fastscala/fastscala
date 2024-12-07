package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.F7Field

import scala.xml.Elem


trait F7FieldInputFieldMixin extends F7Field {

  def processInputElem(input: Elem): Elem = input
}
