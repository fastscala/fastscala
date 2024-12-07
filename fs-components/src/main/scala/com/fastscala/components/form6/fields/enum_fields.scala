package com.fastscala.components.form6.fields

object F6EnumField {

  def NonNullable[T <: Enumeration](`enum`: T)(implicit renderer: SelectF6FieldRenderer): F6SelectField[`enum`.Value] =
    new F6SelectField[`enum`.Value](`enum`.values.toList)

  def Nullable[T <: Enumeration](`enum`: T)(implicit renderer: SelectF6FieldRenderer): F6SelectOptField[`enum`.Value] =
    new F6SelectOptField[`enum`.Value]().optionsNonEmpty(`enum`.values.toList)

  def Multi[T <: Enumeration](`enum`: T)(implicit renderer: MultiSelectF6FieldRenderer): F6MultiSelectField[`enum`.Value] =
    new F6MultiSelectField[`enum`.Value]().options(`enum`.values.toList)
}
