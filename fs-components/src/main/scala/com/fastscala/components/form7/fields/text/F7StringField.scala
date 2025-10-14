package com.fastscala.components.form7.fields.text

import com.fastscala.components.form7.F7Field
import com.fastscala.components.form7.fields.F7ValueEncodedAsStringFieldBase
import com.fastscala.components.form7.renderers.*

import scala.xml.NodeSeq


class F7StringField()(implicit renderer: TextF7FieldRenderer) extends F7ValueEncodedAsStringFieldBase[String] {

  override def defaultValue: String = ""

  def toString(value: String): String = value

  def fromString(str: String): Either[String, String] = Right(str)

  override def validate(): Seq[(F7Field, NodeSeq)] = super.validate() ++
    (if (_required() && currentValue.trim == "") Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())
}
