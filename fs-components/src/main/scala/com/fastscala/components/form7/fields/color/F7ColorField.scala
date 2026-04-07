package com.fastscala.components.form7.fields.color

import com.fastscala.components.form7.fields.F7InputFieldBase
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.mixins.mainelem.*
import com.fastscala.components.form7.renderers.*

import java.util.regex.Pattern

class F7ColorField()(implicit renderer: F7InputFieldRenderer) extends F7InputFieldBase[String] with F7FieldWithPrefix with F7FieldWithSuffix with F7FieldWithMin with F7FieldWithStep with F7FieldWithMax {

  override def _inputTypeDefault: String = "color"

  override def defaultValue: String = "#000000"

  def toString(value: String): String = value.trim

  def fromString(str: String): Either[String, String] = Right(str)
}
