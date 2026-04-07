package com.fastscala.components.form7.fields.number

import com.fastscala.components.form7.F7Field
import com.fastscala.components.form7.fields.{F7InputFieldBase, F7InputOptFieldBase}
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.mixins.mainelem.*
import com.fastscala.components.form7.renderers.*

import java.text.DecimalFormat
import java.util.regex.Pattern
import scala.xml.NodeSeq


class F7IntOptField()(implicit renderer: F7InputFieldRenderer) extends F7NumericOptFieldBase[Int] {

  def toString(value: Option[Int]): String = value.map(value => prefix + " " + new DecimalFormat("0.#").format(value) + " " + suffix).map(_.trim).getOrElse("")

  def fromString(str: String): Either[String, Option[Int]] = {
    if (str.trim == "") {
      Right(None)
    } else {
      str
        .toLowerCase
        .trim
        .replaceAll("^" + Pattern.quote(prefix.toLowerCase) + " *", "")
        .replaceAll(" *" + Pattern.quote(suffix.toLowerCase) + "$", "")
        .toIntOption match {
        case Some(value) => Right(Some(value))
        case None => Left(s"Not an Int?: $str")
      }
    }
  }
}
