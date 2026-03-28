package com.fastscala.components.form7.fields.number

import com.fastscala.components.form7.fields.F7InputFieldBase
import com.fastscala.components.form7.mixins.{F7FieldWithMax, F7FieldWithMin, F7FieldWithPrefix, F7FieldWithStep, F7FieldWithSuffix}
import com.fastscala.components.form7.renderers.{F7InputValidatableFieldRenderer, TextF7FieldRenderer}

trait F7NumericFieldBase[T]()(implicit renderer: TextF7FieldRenderer)
  extends F7InputFieldBase[T]
    with F7FieldWithPrefix
    with F7FieldWithSuffix
    with F7FieldWithMin
    with F7FieldWithStep
    with F7FieldWithMax {

  override def _inputTypeDefault: String = "number"

}
