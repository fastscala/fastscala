package com.fastscala.components.form7.fields.text

import com.fastscala.components.form7.renderers.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter


class F7LocalDateField(dflt: LocalDate = LocalDate.now())(implicit renderer: TextF7FieldRenderer) extends F7TextFieldBase[java.time.LocalDate] {
  override def _inputTypeDefault: String = "date"

  override def defaultValue: LocalDate = dflt

  def toString(value: java.time.LocalDate): String = value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

  def fromString(str: String): Either[String, java.time.LocalDate] = scala.util.Try(java.time.LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd"))).toEither.left.map(_ => "Invalid input")
}
