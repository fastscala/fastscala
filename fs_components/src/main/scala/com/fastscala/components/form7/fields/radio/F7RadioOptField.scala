package com.fastscala.components.form7.fields.radio

import com.fastscala.components.form7.F7Field
import com.fastscala.components.form7.renderers.*

import scala.xml.NodeSeq

class F7RadioOptField[T]()(implicit renderer: RadioF7FieldRenderer) extends F7RadioFieldBase[Option[T]] {
  override def defaultValue: Option[T] = None

  def optionsNonEmpty(v: Seq[T]): F7RadioOptField.this.type = options(None +: v.map(Some(_)))

  override def validate(): Seq[(F7Field, NodeSeq)] = super.validate() ++
    (if (required && currentValue.isEmpty) Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())
}