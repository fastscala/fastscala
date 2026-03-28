package com.fastscala.components.bootstrap5.form7.renderers

import com.fastscala.components.aceeditor.AceEditor
import com.fastscala.components.bootstrap5.form7.BSStandardF7ModifiableFieldRenderer
import com.fastscala.components.form7.fields.aceeditor.F7AceEditorField
import com.fastscala.components.form7.mixins.F7FieldWithValidation
import com.fastscala.components.form7.renderers.{F7AceEditorValidatableFieldRenderer, F7InputValidatableFieldRenderer}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.{inScriptTag, printBeforeExec}

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

trait BSAceEditorF7FieldRenderer extends F7AceEditorValidatableFieldRenderer with BSStandardF7ModifiableFieldRenderer {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  override def render(field: F7AceEditorField)(aceEditor: AceEditor, label: Option[Elem], invalidFeedback: Option[Elem], validFeedback: Option[Elem], help: Option[Elem])(implicit fsc: FSContext): Elem =  {
    val aroundId = field.aroundId
    val aceEditorRendered = aceEditor.render()
    val aceEditorId = aceEditorRendered.getIdOpt.getOrElse(field.elemId)
    val labelId = label.flatMap(_.getIdOpt).getOrElse(field.labelId)
    val invalidFeedbackId = invalidFeedback.flatMap(_.getIdOpt).getOrElse(field.invalidFeedbackId)
    val validFeedbackId = validFeedback.flatMap(_.getIdOpt).getOrElse(field.validFeedbackId)
    val helpId = help.flatMap(_.getIdOpt).getOrElse(field.helpId)

    div.mb_3.withId(aroundId).apply {
      (label.map(_.withIdIfNotSet(labelId).form_label.withFor(aceEditorId).pipe(onLabelTransforms)).getOrElse(Empty) ++
        aceEditorRendered.withIdIfNotSet(field.elemId).pipe(onInputElemTransforms) ++
        invalidFeedback.getOrElse(div.visually_hidden).invalid_feedback.withFor(field.elemId).withIdIfNotSet(invalidFeedbackId).pipe(onInvalidFeedbackTransforms) ++
        validFeedback.getOrElse(div.visually_hidden).valid_feedback.withIdIfNotSet(validFeedbackId).pipe(onValidFeedbackTransforms) ++
        help.getOrElse(div.visually_hidden).form_text.withIdIfNotSet(helpId).pipe(onHelpTransforms) ++
        aceEditor.initialize().onDOMContentLoaded.inScriptTag
        : NodeSeq)
    }.pipe(onAroundDivTransforms)
  }
}
