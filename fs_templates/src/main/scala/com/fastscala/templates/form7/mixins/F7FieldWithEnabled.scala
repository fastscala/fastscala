package com.fastscala.templates.form7.mixins

import com.fastscala.templates.utils.Mutable


trait F7FieldWithEnabled extends Mutable {

  var _enabled: () => Boolean = () => true

  def enabled(): Boolean = _enabled()

  def isEnabled: this.type = enabled(true)

  def isNotEnabled: this.type = enabled(false)

  def enabled(v: Boolean): this.type = mutate {
    _enabled = () => v
  }

  def enabled(f: () => Boolean): this.type = mutate {
    _enabled = f
  }
}
