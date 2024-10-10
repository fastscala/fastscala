package com.fastscala.templates.form7.fields.radio

import com.fastscala.templates.form7.mixins._
import com.fastscala.templates.form7.renderers._


class F7RadioField[T](opts: () => Seq[T])(implicit renderer: RadioF7FieldRenderer) extends F7RadioFieldBase[T] with F7FieldWithValidations {
  options(opts)

  def this(opts: Seq[T])(implicit renderer: RadioF7FieldRenderer) = this(() => opts)

  override def defaultValue: T = options.head
}
