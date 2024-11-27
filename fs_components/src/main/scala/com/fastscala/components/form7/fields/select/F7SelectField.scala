package com.fastscala.components.form7.fields.select

import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.renderers.*


class F7SelectField[T](opts: () => Seq[T])(implicit renderer: SelectF7FieldRenderer) extends F7SelectFieldBase[T] with F7FieldWithValidations {
  options(opts)

  def this(opts: Seq[T])(implicit renderer: SelectF7FieldRenderer) = this(() => opts)

  override def defaultValue: T = options.head
}
