package com.fastscala.templates.form7.renderers

import com.fastscala.templates.form7.RenderHint

import scala.xml.{Elem, NodeSeq}

trait SelectF7FieldRenderer extends StandardOneInputElemF7FieldRenderer {

  def renderOption(
                    selected: Boolean,
                    value: String,
                    label: NodeSeq
                  )(implicit hints: Seq[RenderHint]): Elem
}
