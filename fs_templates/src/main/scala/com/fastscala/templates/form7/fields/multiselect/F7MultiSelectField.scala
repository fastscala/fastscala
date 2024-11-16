package com.fastscala.templates.form7.fields.multiselect

import com.fastscala.templates.form7.renderers.*

class F7MultiSelectField[T]()(implicit renderer: MultiSelectF7FieldRenderer) extends F7MultiSelectFieldBase[T] {
  override def defaultValue: Set[T] = Set()
}
