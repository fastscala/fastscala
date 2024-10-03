package com.fastscala.templates.form7.mixins

import com.fastscala.templates.form7.F7Field
import com.fastscala.templates.utils.Mutable


trait F7FieldWithDependencies extends Mutable {
  var _deps: () => Set[F7Field] = () => Set()

  def deps() = _deps()

  def deps(v: F7Field*): this.type = deps(v.toSet)

  def deps(v: Set[F7Field]): this.type = mutate {
    _deps = () => v
  }

  def deps(f: () => Set[F7Field]): this.type = mutate {
    _deps = f
  }
}
