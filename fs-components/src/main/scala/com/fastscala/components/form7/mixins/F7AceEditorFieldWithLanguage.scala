package com.fastscala.components.form7.mixins

import com.fastscala.components.aceeditor.Language
import com.fastscala.components.form7.{F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7AceEditorFieldWithLanguage extends F7FieldInputFieldMixin with Mutable {
  private val _language: F7FieldMixinStatus[Language.Value] = F7FieldMixinStatus(Language.text)

  def language: Language.Value = _language()

  def language(v: Language.Value): this.type = mutate {
    _language() = () => v
  }
}
