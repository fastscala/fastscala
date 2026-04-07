package com.fastscala.components.form7.fields.number

import com.fastscala.components.form7.fields.F7InputFieldBase
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.mixins.mainelem.*
import com.fastscala.components.form7.renderers.*

import java.util.regex.Pattern


class F7IntField()(implicit renderer: F7InputFieldRenderer) extends F7NumericFieldBase[Int] {

  override def defaultValue: Int = 0

  def toString(value: Int): String = (prefix + " " + value + " " + suffix).trim

  def fromString(str: String): Either[String, Int] = {
    str
      .toLowerCase
      .trim
      .replaceAll("^" + Pattern.quote(prefix.toLowerCase) + " *", "")
      .replaceAll(" *" + Pattern.quote(suffix.toLowerCase) + "$", "")
      .toIntOption match {
      case Some(value) => Right(value)
      case None => Left(s"Not an Int?: $str")
    }
  }
}
