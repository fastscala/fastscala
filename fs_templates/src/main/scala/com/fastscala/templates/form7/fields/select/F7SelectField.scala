package com.fastscala.templates.form7.fields.select

import com.fastscala.templates.form7.mixins._
import com.fastscala.templates.form7.renderers._


class F7SelectField[T](opts: () => Seq[T])(implicit renderer: SelectF7FieldRenderer) extends F7SelectFieldBase[T] with F7FieldWithValidations {
  options(opts)

  def this(opts: Seq[T])(implicit renderer: SelectF7FieldRenderer) = this(() => opts)

  override def defaultValue: T = options.head
}
