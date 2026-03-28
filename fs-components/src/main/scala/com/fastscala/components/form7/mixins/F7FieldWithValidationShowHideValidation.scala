package com.fastscala.components.form7.mixins

import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.form7.renderers.{F7InputValidatableFieldRenderer, F7ValidatableFieldRenderer}
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.NodeSeq


trait F7FieldWithValidationShowHideValidation extends F7FieldWithValidation {

  def renderer: F7ValidatableFieldRenderer

  def showOrUpdateValidation(ns: NodeSeq): Js = renderer.showOrUpdateValidation(this)(ns)

  def hideValidation(): Js = renderer.hideValidation(this)()
}
