package com.fastscala.components.form7.fields.number

import com.fastscala.components.form7.fields.F7InputFieldBase
import com.fastscala.components.form7.mixins.mainelem.*
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.renderers.{F7ValidatableFieldWithMainElemRenderer, F7InputFieldRenderer}

trait F7NumericFieldBase[T]()(implicit renderer: F7InputFieldRenderer)
  extends F7InputFieldBase[T]
    with F7FieldWithPrefix
    with F7FieldWithSuffix
    with F7FieldWithMin
    with F7FieldWithStep
    with F7FieldWithMax {

  override def _inputTypeDefault: String = "number"

}
