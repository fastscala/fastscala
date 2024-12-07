package com.fastscala.components.form7.fields.text

import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.renderers.*

import java.util.regex.Pattern


class F7DoubleField()(implicit renderer: TextF7FieldRenderer)
  extends F7TextFieldBase[Double]
    with F7FieldWithPrefix
    with F7FieldWithSuffix
    with F7FieldWithMin
    with F7FieldWithStep
    with F7FieldWithMax {

  override def _inputTypeDefault: String = "number"

  override def defaultValue: Double = 0

  def toString(value: Double): String = (prefix + " " + value.formatted("%.2f") + " " + suffix).trim

  def fromString(str: String): Either[String, Double] = {
    str
      .toLowerCase
      .trim
      .replaceAll("^" + Pattern.quote(prefix.toLowerCase) + " *", "")
      .replaceAll(" *" + Pattern.quote(suffix.toLowerCase) + "$", "")
      .toDoubleOption match {
      case Some(value) => Right(value)
      case None => Left(s"Not a double?: $str")
    }
  }
}
