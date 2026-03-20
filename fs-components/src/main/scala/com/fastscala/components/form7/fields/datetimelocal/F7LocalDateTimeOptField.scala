package com.fastscala.components.form7.fields.datetimelocal

import com.fastscala.components.form7.F7Field
import com.fastscala.components.form7.fields.{F7InputFieldBase, F7InputOptFieldBase}
import com.fastscala.components.form7.renderers.*

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.xml.NodeSeq


class F7LocalDateTimeOptField()(implicit renderer: TextF7FieldRenderer) extends F7InputOptFieldBase[java.time.LocalDateTime] {
  override def _inputTypeDefault: String = "datetime-local"

  def toString(value: Option[java.time.LocalDateTime]): String = value.map(_.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))).getOrElse("")

  def fromString(str: String): Either[String, Option[java.time.LocalDateTime]] = Right(Some(str).filter(_.trim != "").map(str => java.time.LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))))
}
