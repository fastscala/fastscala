package com.fastscala.components.form7.mixins

import com.fastscala.components.utils.Mutable


trait F7FieldWithSubmitOnEnter extends Mutable {

  var _submitOnEnter: () => Boolean = () => true

  def submitOnEnter: Boolean = _submitOnEnter()

  def issubmitOnEnter: this.type = submitOnEnter(true)

  def isNotsubmitOnEnter: this.type = submitOnEnter(false)

  def submitOnEnter(v: Boolean): this.type = mutate {
    _submitOnEnter = () => v
  }

  def submitOnEnter(f: () => Boolean): this.type = mutate {
    _submitOnEnter = f
  }
}
