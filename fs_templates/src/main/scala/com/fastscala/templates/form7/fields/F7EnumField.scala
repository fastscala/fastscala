package com.fastscala.templates.form7.fields

import com.fastscala.templates.form7.fields.multiselect.F7MultiSelectField
import com.fastscala.templates.form7.fields.select.*
import com.fastscala.templates.form7.renderers.{MultiSelectF7FieldRenderer, SelectF7FieldRenderer}

object F7EnumField {

  def NonNullable[T <: Enumeration](`enum`: T)(implicit renderer: SelectF7FieldRenderer): F7SelectField[`enum`.Value] =
    new F7SelectField[`enum`.Value](`enum`.values.toList)

  def Nullable[T <: Enumeration](`enum`: T)(implicit renderer: SelectF7FieldRenderer): F7SelectOptField[`enum`.Value] =
    new F7SelectOptField[`enum`.Value]().optionsNonEmpty(`enum`.values.toList)

  def Multi[T <: Enumeration](`enum`: T)(implicit renderer: MultiSelectF7FieldRenderer): F7MultiSelectField[`enum`.Value] =
    new F7MultiSelectField[`enum`.Value]().options(`enum`.values.toList)
}
