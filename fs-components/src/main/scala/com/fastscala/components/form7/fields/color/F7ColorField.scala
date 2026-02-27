package com.fastscala.components.form7.fields.color

import com.fastscala.components.form7.fields.F7ValueEncodedAsStringFieldBase
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.renderers.*

import java.util.regex.Pattern

class F7ColorField()(implicit renderer: TextF7FieldRenderer) extends F7ValueEncodedAsStringFieldBase[String] with F7FieldWithPrefix with F7FieldWithSuffix with F7FieldWithMin with F7FieldWithStep with F7FieldWithMax {

  override def _inputTypeDefault: String = "color"

  override def defaultValue: String = "#000000"

  def toString(value: String): String = value.trim

  def fromString(str: String): Either[String, String] = Right(str)
}
