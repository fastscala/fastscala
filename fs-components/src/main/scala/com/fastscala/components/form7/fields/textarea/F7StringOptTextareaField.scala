package com.fastscala.components.form7.fields.text

import com.fastscala.components.form7.F7Field
import com.fastscala.components.form7.mixins.F7FieldWithValueOpt
import com.fastscala.components.form7.renderers.*

import scala.xml.NodeSeq

class F7StringOptTextareaField()(implicit renderer: TextareaF7FieldRenderer) extends F7TextareaFieldBase[Option[String]] with F7FieldWithValueOpt[String] {

  def toString(value: Option[String]): String = value.getOrElse("")

  def fromString(str: String): Either[String, Option[String]] = Right(Some(str).filter(_ != ""))
}

