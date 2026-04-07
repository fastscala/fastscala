package com.fastscala.components.form7.fields.number

import com.fastscala.components.form7.fields.F7InputFieldBase
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.mixins.mainelem.*
import com.fastscala.components.form7.renderers.*

import java.util.regex.Pattern


class F7DoubleField()(implicit renderer: F7InputFieldRenderer) extends F7NumericFieldBase[Double] {

  override def defaultValue: Double = 0

  def toString(value: Double): String = (prefix + " " + value.toString + " " + suffix).trim

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
