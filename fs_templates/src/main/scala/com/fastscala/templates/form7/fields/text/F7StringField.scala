package com.fastscala.templates.form7.fields.text

import com.fastscala.templates.form7.F7Field
import com.fastscala.templates.form7.renderers._
import com.fastscala.xml.scala_xml.FSScalaXmlSupport

import scala.xml.NodeSeq


class F7StringField()(implicit renderer: TextF7FieldRenderer) extends F7TextFieldBase[String] {

  override def defaultValue: String = ""

  def toString(value: String): String = value

  def fromString(str: String): Either[String, String] = Right(str)

  override def validate(): Seq[(F7Field, NodeSeq)] = super.validate() ++
    (if (_required() && currentValue.trim == "") Seq((this, FSScalaXmlSupport.fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}
