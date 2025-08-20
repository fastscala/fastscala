package com.fastscala.components.form7.renderers


import com.fastscala.components.form7.RenderHint

import scala.xml.{Elem, NodeSeq}

trait MultiSelectF7FieldRenderer extends StandardOneInputElemF7FieldRenderer {

  def renderOption(
                    selected: Boolean,
                    value: String,
                    label: NodeSeq
                  ): Elem
}