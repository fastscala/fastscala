package com.fastscala.templates.form7.renderers

import com.fastscala.templates.form7.RenderHint
import com.fastscala.templates.form7.fields.text.F7TextareaField

import scala.xml.{Elem, NodeSeq}

trait TextareaF7FieldRenderer {

  def defaultRequiredFieldLabel: String

  def render[T](field: F7TextareaField[T])(label: Option[NodeSeq], inputElem: Elem, error: Option[NodeSeq])(implicit hints: Seq[RenderHint]): Elem
}
