package com.fastscala.templates.form6.fields

object EnumField {

  def NonNullable[T <: Enumeration](enum: T)(implicit renderer: SelectF6FieldRenderer): F6SelectField[T#Value] =
    new F6SelectField[T#Value](`enum`.values.toList)

  def Nullable[T <: Enumeration](enum: T)(implicit renderer: SelectF6FieldRenderer): F6SelectOptField[T#Value] =
    new F6SelectOptField[T#Value]().optionsNonEmpty(`enum`.values.toList)

  def Multi[T <: Enumeration](enum: T)(implicit renderer: MultiSelectF6FieldRenderer): F6MultiSelectField[T#Value] =
    new F6MultiSelectField[T#Value]().options(`enum`.values.toList)
}
