package com.fastscala.components.form7.fields.text

import com.fastscala.components.form7.F7Field
import com.fastscala.components.form7.fields.F7ValueEncodedAsStringFieldBase
import com.fastscala.components.form7.renderers.*

import scala.xml.NodeSeq


class F7StringOptField()(implicit renderer: TextF7FieldRenderer) extends F7ValueEncodedAsStringFieldBase[Option[String]] {

  override def defaultValue: Option[String] = None

  def toString(value: Option[String]): String = value.getOrElse("")

  def fromString(str: String): Either[String, Option[String]] = Right(Some(str).filter(_ != ""))

  override def validate(): Seq[(F7Field, NodeSeq)] = super.validate() ++
    (if (required && currentValue.isEmpty) Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())
}
