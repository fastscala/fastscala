package com.fastscala.components.form7.fields.radio

import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.renderers.*


class F7RadioField[T](opts: () => Seq[T])(implicit renderer: RadioF7FieldRenderer) extends F7RadioFieldBase[T] with F7FieldWithValidations {
  options(opts)

  def this(opts: Seq[T])(implicit renderer: RadioF7FieldRenderer) = this(() => opts)

  override def defaultValue: T = options.head
}
