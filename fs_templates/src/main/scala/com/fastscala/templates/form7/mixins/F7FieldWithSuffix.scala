package com.fastscala.templates.form7.mixins

import com.fastscala.templates.utils.Mutable


trait F7FieldWithSuffix extends Mutable {

  var _suffix: () => String = () => ""

  def suffix() = _suffix()

  def suffix(v: String): this.type = mutate {
    _suffix = () => v
  }

  def suffix(f: () => String): this.type = mutate {
    _suffix = f
  }
}
