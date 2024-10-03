package com.fastscala.templates.form7.renderers

import com.fastscala.templates.form7.RenderHint
import com.fastscala.templates.form7.fields.F7CheckboxField

import scala.xml.{Elem, NodeSeq}


trait CheckboxF7FieldRenderer {

  def render(field: F7CheckboxField)(label: Option[Elem], elem: Elem, error: Option[NodeSeq])(implicit hints: Seq[RenderHint]): Elem
}
