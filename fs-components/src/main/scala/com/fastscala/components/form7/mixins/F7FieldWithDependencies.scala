package com.fastscala.components.form7.mixins

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.form7.*
import com.fastscala.components.utils.Mutable
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem


trait F7FieldWithDependencies extends Mutable {
  var _deps: () => Set[F7Field] = () => Set()

  def deps = _deps()

  def dependsOn(v: F7Field*): this.type = deps(v.toSet)

  def deps(v: Set[F7Field]): this.type = mutate {
    _deps = () => v
  }

  def deps(f: () => Set[F7Field]): this.type = mutate {
    _deps = f
  }
}
