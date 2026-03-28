package com.fastscala.components.form7.mixins

import com.fastscala.components.aceeditor.Theme
import com.fastscala.components.form7.{F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7AceEditorFieldWithTheme extends F7FieldInputFieldMixin with Mutable {
  private val _theme: F7FieldMixinStatus[Theme.Value] = F7FieldMixinStatus(Theme.textmate)

  def theme: Theme.Value = _theme()

  def theme(v: Theme.Value): this.type = mutate {
    _theme() = () => v
  }
}
