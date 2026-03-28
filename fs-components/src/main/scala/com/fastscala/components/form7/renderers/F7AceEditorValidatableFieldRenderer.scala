package com.fastscala.components.form7.renderers

import com.fastscala.components.aceeditor.AceEditor
import com.fastscala.components.form7.fields.aceeditor.F7AceEditorField
import com.fastscala.components.form7.mixins.F7FieldWithValidation
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.xml.{Elem, NodeSeq}

trait F7AceEditorValidatableFieldRenderer extends F7ValidatableFieldRenderer {

  def render(field: F7AceEditorField)(
    aceEditor: AceEditor,
    label: Option[Elem],
    invalidFeedback: Option[Elem],
    validFeedback: Option[Elem],
    help: Option[Elem],
  )(implicit fsc: FSContext): Elem
}


