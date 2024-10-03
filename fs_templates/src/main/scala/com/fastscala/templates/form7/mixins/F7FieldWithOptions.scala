package com.fastscala.templates.form7.mixins

import com.fastscala.templates.form7.fields.F7DefaultField


trait F7FieldWithOptions[T] extends F7DefaultField {
  var _options: () => Seq[T] = () => Nil

  def options() = _options()

  def options(v: Seq[T]): this.type = mutate {
    _options = () => v
  }

  def options(f: () => Seq[T]): this.type = mutate {
    _options = f
  }
}
