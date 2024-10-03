package com.fastscala.templates.form7.renderers

import com.fastscala.templates.form7.RenderHint
import com.fastscala.templates.form7.fields.text.F7TextField

import scala.xml.{Elem, NodeSeq}


trait TextF7FieldRenderer {

  def defaultRequiredFieldLabel: String

  def render[T](field: F7TextField[T])(label: Option[NodeSeq], inputElem: Elem, error: Option[NodeSeq])(implicit hints: Seq[RenderHint]): Elem
}


