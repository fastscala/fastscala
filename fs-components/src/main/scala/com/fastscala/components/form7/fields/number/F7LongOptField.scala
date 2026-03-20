package com.fastscala.components.form7.fields.number

import com.fastscala.components.form7.F7Field
import com.fastscala.components.form7.fields.{F7InputFieldBase, F7InputOptFieldBase}
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.renderers.*

import java.text.DecimalFormat
import java.util.regex.Pattern
import scala.xml.NodeSeq


class F7LongOptField()(implicit renderer: TextF7FieldRenderer) extends F7NumericOptFieldBase[Long] {

  def toString(value: Option[Long]): String = value.map(value => prefix + " " + new DecimalFormat("0.#").format(value) + " " + suffix).map(_.trim).getOrElse("")

  def fromString(str: String): Either[String, Option[Long]] = {
    if (str.trim == "") {
      Right(None)
    } else {
      str
        .toLowerCase
        .trim
        .replaceAll("^" + Pattern.quote(prefix.toLowerCase) + " *", "")
        .replaceAll(" *" + Pattern.quote(suffix.toLowerCase) + "$", "")
        .toLongOption match {
        case Some(value) => Right(Some(value))
        case None => Left(s"Not a Long?: $str")
      }
    }
  }
}
