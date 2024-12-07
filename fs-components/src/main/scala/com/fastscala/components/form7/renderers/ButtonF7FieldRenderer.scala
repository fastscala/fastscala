package com.fastscala.components.form7.renderers

import com.fastscala.components.form7.RenderHint
import com.fastscala.components.form7.fields.F7SubmitButtonField

import scala.xml.Elem


trait ButtonF7FieldRenderer {
  def render(field: F7SubmitButtonField[_])(btn: Elem)(implicit hints: Seq[RenderHint]): Elem
}
