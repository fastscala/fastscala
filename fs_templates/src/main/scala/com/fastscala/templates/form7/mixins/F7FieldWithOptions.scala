package com.fastscala.templates.form7.mixins

import com.fastscala.templates.utils.Mutable


trait F7FieldWithOptions[T] extends Mutable {
  var _options: () => Seq[T] = () => Nil

  def options: Seq[T] = _options()

  def options(v: Seq[T]): this.type = mutate {
    _options = () => v
  }

  def options(f: () => Seq[T]): this.type = mutate {
    _options = f
  }
}
