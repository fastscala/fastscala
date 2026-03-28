package com.fastscala.components.form7.fields.select

import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.renderers.*


class F7SelectField[T](opts: () => Seq[T])(implicit renderer: F7SelectFieldRenderer) extends F7SelectFieldBase[T] with F7FieldWithValidationRules {
  options(opts)

  def this(opts: Seq[T])(implicit renderer: F7SelectFieldRenderer) = this(() => opts)

  override def defaultValue: T = options.head
}
