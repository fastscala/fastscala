package com.fastscala.templates.form7.mixins

import com.fastscala.templates.utils.Mutable


trait F7FieldWithPrefix extends Mutable {

  var _prefix: () => String = () => ""

  def prefix() = _prefix()

  def prefix(v: String): this.type = mutate {
    _prefix = () => v
  }

  def prefix(f: () => String): this.type = mutate {
    _prefix = f
  }
}
