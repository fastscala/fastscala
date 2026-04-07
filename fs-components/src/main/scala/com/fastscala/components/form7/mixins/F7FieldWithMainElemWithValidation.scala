package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.mixins.mainelem.F7FieldWithMainElem
import com.fastscala.components.form7.renderers.{F7ValidatableFieldWithMainElemRenderer, ValidationUtils}
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.xml.NodeSeq


trait F7FieldWithMainElemWithValidation extends F7FieldWithMainElem with F7FieldWithValidation {

  def showOrUpdateValidation(ns: NodeSeq): Js = ValidationUtils.F7FieldWithMainElemWithValidation.showOrUpdateValidation(this)(ns)

  def hideValidation(): Js = ValidationUtils.F7FieldWithMainElemWithValidation.hideValidation(this)()
}
