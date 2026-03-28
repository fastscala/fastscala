package com.fastscala.components.form7.renderers

import com.fastscala.components.form7.RenderHint
import com.fastscala.components.form7.fields.submit.F7SubmitButtonField

import scala.xml.Elem


trait F7SubmitButtonFieldRenderer {
  def render(field: F7SubmitButtonField[?])(btn: Elem): Elem
}
