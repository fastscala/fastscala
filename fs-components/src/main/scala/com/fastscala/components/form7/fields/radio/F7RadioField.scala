package com.fastscala.components.form7.fields.radio

import com.fastscala.components.form7.Form7
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.mixins.mainelem.*
import com.fastscala.components.form7.renderers.*
import com.fastscala.core.FSContext
import com.fastscala.js.Js

import scala.util.Try


class F7RadioField[T](opts: () => Seq[T])(implicit renderer: F7RadioFieldRenderer) extends F7RadioFieldBase[T] with F7FieldWithValidationRules {
  options(opts)

  def this(opts: Seq[T])(implicit renderer: F7RadioFieldRenderer) = this(() => opts)

  override def defaultValue: T = options.head
}
