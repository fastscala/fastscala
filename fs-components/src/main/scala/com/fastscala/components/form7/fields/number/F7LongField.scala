package com.fastscala.components.form7.fields.number

import com.fastscala.components.form7.fields.F7InputFieldBase
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.renderers.*

import java.util.regex.Pattern


class F7LongField()(implicit renderer: TextF7FieldRenderer) extends F7NumericFieldBase[Long] {
  
  override def defaultValue: Long = 0L

  def toString(value: Long): String = (prefix + " " + value + " " + suffix).trim

  def fromString(str: String): Either[String, Long] = {
    str
      .toLowerCase
      .trim
      .replaceAll("^" + Pattern.quote(prefix.toLowerCase) + " *", "")
      .replaceAll(" *" + Pattern.quote(suffix.toLowerCase) + "$", "")
      .toLongOption match {
      case Some(value) => Right(value)
      case None => Left(s"Not a Long?: $str")
    }
  }
}
