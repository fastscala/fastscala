package com.fastscala.templates.form7.renderers

import com.fastscala.templates.form7.RenderHint
import com.fastscala.templates.form7.fields.F7SaveButtonField

import scala.xml.Elem


trait ButtonF7FieldRenderer {
  def render(field: F7SaveButtonField[_])(btn: Elem)(implicit hints: Seq[RenderHint]): Elem
}
