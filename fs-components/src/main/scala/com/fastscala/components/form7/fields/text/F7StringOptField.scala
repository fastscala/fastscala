package com.fastscala.components.form7.fields.text

import com.fastscala.components.form7.F7Field
import com.fastscala.components.form7.fields.{F7InputFieldBase, F7InputOptFieldBase}
import com.fastscala.components.form7.renderers.*

import scala.xml.NodeSeq


class F7StringOptField()(implicit renderer: TextF7FieldRenderer) extends F7InputOptFieldBase[String] {

  def toString(value: Option[String]): String = value.getOrElse("")

  def fromString(str: String): Either[String, Option[String]] = Right(Some(str).filter(_ != ""))
}
