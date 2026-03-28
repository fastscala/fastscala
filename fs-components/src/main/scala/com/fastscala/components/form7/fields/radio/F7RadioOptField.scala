package com.fastscala.components.form7.fields.radio

import com.fastscala.components.form7.F7Field
import com.fastscala.components.form7.mixins.F7FieldWithValueOpt
import com.fastscala.components.form7.renderers.*

import scala.xml.NodeSeq

class F7RadioOptField[T]()(implicit renderer: F7RadioFieldRenderer) extends F7RadioFieldBase[Option[T]] with F7FieldWithValueOpt[T] {

  def optionsNonEmpty(v: Seq[T]): F7RadioOptField.this.type = options(None +: v.map(Some(_)))
}