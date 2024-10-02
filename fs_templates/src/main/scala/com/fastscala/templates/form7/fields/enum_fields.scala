package com.fastscala.templates.form7.fields

object F7EnumField {

  def NonNullable[T <: Enumeration](enum: T)(implicit renderer: SelectF7FieldRenderer): F7SelectField[T#Value] =
    new F7SelectField[T#Value](`enum`.values.toList)

  def Nullable[T <: Enumeration](enum: T)(implicit renderer: SelectF7FieldRenderer): F7SelectOptField[T#Value] =
    new F7SelectOptField[T#Value]().optionsNonEmpty(`enum`.values.toList)

  def Multi[T <: Enumeration](enum: T)(implicit renderer: MultiSelectF7FieldRenderer): F7MultiSelectField[T#Value] =
    new F7MultiSelectField[T#Value]().options(`enum`.values.toList)
}
