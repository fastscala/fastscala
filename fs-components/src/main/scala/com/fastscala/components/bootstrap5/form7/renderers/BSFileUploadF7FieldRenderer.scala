package com.fastscala.components.bootstrap5.form7.renderers

import com.fastscala.components.aceeditor.AceEditor
import com.fastscala.components.bootstrap5.form7.BSStandardModifiableF7InputElemFieldRenderer
import com.fastscala.components.bootstrap5.utils.FileUploadDedicatedXmlHttpRequest
import com.fastscala.components.form7.fields.aceeditor.F7AceEditorField
import com.fastscala.components.form7.fields.file.{F7FileUploadField, F7UploadedFile}
import com.fastscala.components.form7.mixins.F7FieldWithValidation
import com.fastscala.components.form7.renderers.{F7AceEditorValidatableFieldRenderer, F7FileUploadFieldRenderer, F7ValidatableFieldWithMainElemRenderer}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.{FSContext, FSUploadedFile}
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.{inScriptTag, printBeforeExec}
import com.fastscala.scala_xml.rerenderers.RerendererP

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

trait BSFileUploadF7FieldRenderer extends F7FileUploadFieldRenderer with BSStandardModifiableF7InputElemFieldRenderer {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  override def renderProgressBar(): Elem = <div class="progress-bar progress-bar-striped progress-bar-animated"></div>

  override def renderProgress(progressBar: Elem): Elem = <div class="progress" role="progressbar">{progressBar}</div>

  override def render(field: F7FileUploadField)(
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
        previewRerenderer.render(_ => <div></div>.withStyle("display:none;")) ++
        inputElem.addClass("form-control").withIdIfNotSet(field.mainElemId).pipe(onInputElemTransforms) ++
        invalidFeedback.getOrElse(div.visually_hidden).invalid_feedback.withFor(field.mainElemId).withIdIfNotSet(invalidFeedbackId).pipe(onInvalidFeedbackTransforms) ++
        validFeedback.getOrElse(div.visually_hidden).valid_feedback.withIdIfNotSet(validFeedbackId).pipe(onValidFeedbackTransforms) ++
        help.getOrElse(div.visually_hidden).form_text.withIdIfNotSet(helpId).pipe(onHelpTransforms)
        : NodeSeq)
    }.pipe(onAroundDivTransforms)
  }

  //  override def render(field: F7FileUploadField[_])(
  //    processUpload: Seq[FSUploadedFile] => Js,
  //    label: Option[String],
  //    invalidFeedback: Option[Elem],
  //    validFeedback: Option[Elem],
  //    help: Option[Elem]
  //  )(implicit fsc: FSContext): Elem = {
  //    val aroundId = field.aroundId
  //    val aceEditorRendered = aceEditor.render()
  //    val aceEditorId = aceEditorRendered.getIdOpt.getOrElse(field.mainElemId)
  //    val labelId = label.flatMap(_.getIdOpt).getOrElse(field.labelId)
  //    val invalidFeedbackId = invalidFeedback.flatMap(_.getIdOpt).getOrElse(field.invalidFeedbackId)
  //    val validFeedbackId = validFeedback.flatMap(_.getIdOpt).getOrElse(field.validFeedbackId)
  //    val helpId = help.flatMap(_.getIdOpt).getOrElse(field.helpId)
  //
  //    val fileUpload = {
  //      val actionUrl = fsc.fileUploadActionUrl({
  //        case uploadedFile => processUpload(uploadedFile)
  //      })
  //      val progressId = IdGen.id("progress")
  //      val progressBarId = IdGen.id("progressBar")
  //      val inputId = IdGen.id("input")
  //      val buttonId = IdGen.id("btn")
  //      ScalaXmlElemUtils.showIf(clipboardUploadEnabled)(FileUpload.pasteFromClipboard(processUpload)) ++
  //        <form>
  //          {
  //          labelOpt.map(label => label.withFor(inputId)).getOrElse(Empty)
  //          }
  //          <input class={inputClasses} name="file" type="file" accept={acceptTypes.getOrElse(null)} multiple={Some("true").filter(_ => multiple).getOrElse(null)} id={inputId} onchange={JS.show(buttonId).cmd} />
  //          {
  //          transformProgress(<div class="progress" role="progressbar">{transformProgressBar(<div class="progress-bar progress-bar-striped progress-bar-animated"></div>.withId(progressBarId))}</div>.withId(progressId).withStyle("display:none;"))
  //          }
  //          {
  //          transformSubmitBtn(
  //            BSBtn().BtnPrimary.sm.id(buttonId).withStyle("display:none").lbl(submitBtnLbl.getOrElse("")).onclick(JS.hide(buttonId) & JS.show(progressId) & Js(
  //              s"""
  //                 |var fileInput = document.getElementById(${JS.asJsStr(inputId)});
  //                 |var formdata = new FormData();
  //                 |for (var i = 0; i < fileInput.files.length; i++) {
  //                 |    formdata.append('file', fileInput.files[i]);
  //                 |}
  //                 |var request = new XMLHttpRequest();
  //                 |request.upload.addEventListener('progress', function (e) {
  //                 |    if (e.lengthComputable) {
  //                 |        var percent = Math.round((e.loaded / e.total) * 100);
  //                 |
  //                 |        var progressBar = document.getElementById(${JS.asJsStr(progressBarId)});
  //                 |        progressBar.style.display = "block";
  //                 |        progressBar.style.width = percent + '%';
  //                 |        progressBar.innerHTML = percent + '%';
  //                 |    }
  //                 |});
  //                 |request.onload = function() { try {eval(this.responseText);} catch(err) { console.log(err.message); console.log('While runnning the code:\\n' + this.responseText); } };
  //                 |
  //                 |request.open('post', ${JS.asJsStr(actionUrl)});
  //                 |request.timeout = $uploadTimeoutMillis;
  //                 |request.send(formdata);
  //                 |""".stripMargin
  //            )).btn
  //          )
  //          }
  //        </form>
  //    }
  //
  //    div.mb_3.withId(aroundId).apply {
  //      (label.map(_.withIdIfNotSet(labelId).form_label.withFor(aceEditorId).pipe(onLabelTransforms)).getOrElse(Empty) ++
  //        aceEditorRendered.withIdIfNotSet(field.mainElemId).pipe(onInputElemTransforms) ++
  //        invalidFeedback.getOrElse(div.visually_hidden).invalid_feedback.withFor(field.mainElemId).withIdIfNotSet(invalidFeedbackId).pipe(onInvalidFeedbackTransforms) ++
  //        validFeedback.getOrElse(div.visually_hidden).valid_feedback.withIdIfNotSet(validFeedbackId).pipe(onValidFeedbackTransforms) ++
  //        help.getOrElse(div.visually_hidden).form_text.withIdIfNotSet(helpId).pipe(onHelpTransforms) ++
  //        aceEditor.initialize().onDOMContentLoaded.inScriptTag
  //        : NodeSeq)
  //    }.pipe(onAroundDivTransforms)
  //  }
}
