package com.fastscala.components.form7.mixins

import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.form7.renderers.F7InputValidatableFieldRenderer
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.NodeSeq


trait F7InputValidatableField extends ValidatableF7Field {

  def renderer: F7InputValidatableFieldRenderer

  def showOrUpdateValidation(ns: NodeSeq): Js = renderer.showOrUpdateValidation(this)(ns)

  def hideValidation(): Js = renderer.hideValidation(this)()
}
