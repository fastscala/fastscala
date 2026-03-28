package com.fastscala.components.form7.renderers


import com.fastscala.components.form7.RenderHint
import com.fastscala.components.form7.fields.multiselect.{F7MultiSelectField, F7MultiSelectFieldBase}
import com.fastscala.components.form7.mixins.F7FieldWithValidation

import scala.xml.{Elem, NodeSeq}

trait F7MultiSelectFieldRenderer extends F7InputValidatableFieldRenderer {

  def renderOption(
                    selected: Boolean,
                    value: String,
                    label: NodeSeq
                  ): Elem
}