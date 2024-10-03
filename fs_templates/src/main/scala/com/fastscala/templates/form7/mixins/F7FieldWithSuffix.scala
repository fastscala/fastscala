package com.fastscala.templates.form7.mixins

import com.fastscala.templates.form7.fields.F7DefaultField


trait F7FieldWithSuffix extends F7DefaultField {

  var _suffix: () => String = () => ""

  def suffix() = _suffix()

  def suffix(v: String): this.type = mutate {
    _suffix = () => v
  }

  def suffix(f: () => String): this.type = mutate {
    _suffix = f
  }
}
