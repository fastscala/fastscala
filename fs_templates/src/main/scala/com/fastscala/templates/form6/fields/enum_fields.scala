package com.fastscala.templates.form6.fields

import com.fastscala.core.{FSXmlEnv, FSXmlSupport}

object EnumField {

  def NonNullable[E <: FSXmlEnv : FSXmlSupport, T <: Enumeration](enum: T)(implicit renderer: SelectF6FieldRenderer[E]): F6SelectField[E, T#Value] =
    new F6SelectField[E, T#Value](`enum`.values.toList)

  def Nullable[E <: FSXmlEnv : FSXmlSupport, T <: Enumeration](enum: T)(implicit renderer: SelectF6FieldRenderer[E]): F6SelectOptField[E, T#Value] =
    new F6SelectOptField[E, T#Value]().optionsValid(`enum`.values.toList)

  def Multi[E <: FSXmlEnv : FSXmlSupport, T <: Enumeration](enum: T)(implicit renderer: MultiSelectF6FieldRenderer[E]): F6MultiSelectField[E, T#Value] =
    new F6MultiSelectField[E, T#Value]().options(`enum`.values.toList)
}
