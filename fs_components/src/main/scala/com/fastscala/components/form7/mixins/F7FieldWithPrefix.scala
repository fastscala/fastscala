package com.fastscala.components.form7.mixins

import com.fastscala.components.utils.Mutable


trait F7FieldWithPrefix extends Mutable {

  var _prefix: () => String = () => ""

  def prefix: String = _prefix()

  def prefix(v: String): this.type = mutate {
    _prefix = () => v
  }

  def prefix(f: () => String): this.type = mutate {
    _prefix = f
  }
}
