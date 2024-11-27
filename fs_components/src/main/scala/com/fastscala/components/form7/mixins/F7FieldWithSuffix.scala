package com.fastscala.components.form7.mixins

import com.fastscala.components.utils.Mutable


trait F7FieldWithSuffix extends Mutable {

  var _suffix: () => String = () => ""

  def suffix: String = _suffix()

  def suffix(v: String): this.type = mutate {
    _suffix = () => v
  }

  def suffix(f: () => String): this.type = mutate {
    _suffix = f
  }
}
