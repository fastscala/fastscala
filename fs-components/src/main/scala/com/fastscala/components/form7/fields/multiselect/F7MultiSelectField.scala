package com.fastscala.components.form7.fields.multiselect

import com.fastscala.components.form7.renderers.*

class F7MultiSelectField[T]()(implicit renderer: F7MultiSelectFieldRenderer) extends F7MultiSelectFieldBase[T] {
  override def defaultValue: Set[T] = Set()
}
