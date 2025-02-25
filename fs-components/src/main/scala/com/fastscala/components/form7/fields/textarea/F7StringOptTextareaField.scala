package com.fastscala.components.form7.fields.text

import com.fastscala.components.form7.F7Field
import com.fastscala.components.form7.renderers.*

import scala.xml.NodeSeq

class F7StringOptTextareaField()(implicit renderer: TextareaF7FieldRenderer) extends F7TextareaFieldBase[Option[String]] {

  override def defaultValue: Option[String] = None

  def toString(value: Option[String]): String = value.getOrElse("")

  def fromString(str: String): Either[String, Option[String]] = Right(Some(str).filter(_ != ""))

  override def validate(): Seq[(F7Field, NodeSeq)] = super.validate() ++
    (if (required && currentValue.isEmpty) Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())
}

