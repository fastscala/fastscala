package com.fastscala.templates.form7.mixins

import com.fastscala.js.Js
import com.fastscala.templates.form7.renderers.StandardOneInputElemF7FieldRenderer

import scala.xml.NodeSeq


trait StandardOneInputElemF7Field[T] extends StandardF7Field with F7FieldWithValue[T] {

  def renderer: StandardOneInputElemF7FieldRenderer

  def showOrUpdateValidation(ns: NodeSeq): Js = renderer.showOrUpdateValidation(this)(ns)

  def hideValidation(): Js = renderer.hideValidation(this)()
}
