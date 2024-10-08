package com.fastscala.templates.form7.fields.text

import com.fastscala.templates.form7.F7Field
import com.fastscala.templates.form7.renderers._
import com.fastscala.xml.scala_xml.FSScalaXmlSupport

import scala.xml.NodeSeq

class F7StringTextareaField()(implicit renderer: TextareaF7FieldRenderer) extends F7TextareaFieldBase[String] {

  override def defaultValue: String = ""

  def toString(value: String): String = value

  def fromString(str: String): Either[String, String] = Right(str)

  override def validate(): Seq[(F7Field, NodeSeq)] = super.validate() ++
    (if (required() && currentValue == "") Seq((this, FSScalaXmlSupport.fsXmlSupport.buildText(renderer.defaultRequiredFieldLabel))) else Seq())
}
