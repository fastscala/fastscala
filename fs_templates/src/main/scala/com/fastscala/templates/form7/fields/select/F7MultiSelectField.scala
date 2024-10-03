package com.fastscala.templates.form7.fields.select


import com.fastscala.templates.form7.renderers._

class F7MultiSelectField[T]()(implicit renderer: MultiSelectF7FieldRenderer) extends F7MultiSelectFieldBase[T] {
  override def defaultValue: Set[T] = Set()
}
