package com.fastscala.components.bootstrap5.form7.renderers

import com.fastscala.components.aceeditor.AceEditor
import com.fastscala.components.bootstrap5.form7.BSStandardModifiableF7InputElemFieldRenderer
import com.fastscala.components.bootstrap5.utils.FileUploadDedicatedXmlHttpRequest
import com.fastscala.components.form7.fields.aceeditor.F7AceEditorField
import com.fastscala.components.form7.fields.file.{F7FileField, F7UploadedFile}
import com.fastscala.components.form7.mixins.F7FieldWithValidation
import com.fastscala.components.form7.renderers.{F7AceEditorValidatableFieldRenderer, F7FileFieldRenderer, F7ValidatableFieldWithMainElemRenderer}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.{FSContext, FSUploadedFile}
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.{inScriptTag, printBeforeExec}
import com.fastscala.scala_xml.rerenderers.RerendererP

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

trait BSFileUploadF7FieldRenderer extends F7FileFieldRenderer with BSStandardModifiableF7InputElemFieldRenderer {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  override def renderProgressBar(): Elem = <div class="progress-bar progress-bar-striped progress-bar-animated"></div>

  override def renderProgress(progressBar: Elem): Elem = <div class="progress" role="progressbar">{progressBar}</div>

  override def render(field: F7FileField)(
    inputElem: Elem,
    previewRerenderer: RerendererP[FSContext => Elem],
    progressElem: Elem,
    label: Option[Elem],
    invalidFeedback: Option[Elem],
    validFeedback: Option[Elem],
    help: Option[Elem]
  )(implicit fsc: FSContext): Elem = {
    val aroundId = field.aroundId
    val labelId = label.flatMap(_.getIdOpt).getOrElse(field.labelId)
    val invalidFeedbackId = invalidFeedback.flatMap(_.getIdOpt).getOrElse(field.invalidFeedbackId)
    val validFeedbackId = validFeedback.flatMap(_.getIdOpt).getOrElse(field.validFeedbackId)
    val helpId = help.flatMap(_.getIdOpt).getOrElse(field.helpId)

    div.mb_3.withId(aroundId).apply {
      (label.map(_.withIdIfNotSet(labelId).form_label.withFor(field.mainElemId).pipe(onLabelTransforms)).getOrElse(Empty) ++
        previewRerenderer.render(field.previewRenderer(field.currentValue)) ++
        inputElem.addClass("form-control").withIdIfNotSet(field.mainElemId).pipe(onInputElemTransforms) ++
        invalidFeedback.getOrElse(div.visually_hidden).invalid_feedback.withFor(field.mainElemId).withIdIfNotSet(invalidFeedbackId).pipe(onInvalidFeedbackTransforms) ++
        validFeedback.getOrElse(div.visually_hidden).valid_feedback.withIdIfNotSet(validFeedbackId).pipe(onValidFeedbackTransforms) ++
        help.getOrElse(div.visually_hidden).form_text.withIdIfNotSet(helpId).pipe(onHelpTransforms)
        : NodeSeq)
    }.pipe(onAroundDivTransforms)
  }
}
