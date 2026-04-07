package com.fastscala.components.form7.fields.file

import com.fastscala.components.bootstrap5.helpers.BSHelpers.{Empty, s}
import com.fastscala.components.bootstrap5.utils.{BSBtn, FileUploadDedicatedXmlHttpRequest}
import com.fastscala.components.form7.fields.F7InputFieldBase
import com.fastscala.components.form7.mixins.mainelem.*
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.renderers.*
import com.fastscala.components.form7.{ChangedField, F7Field, Form7, SuggestSubmit}
import com.fastscala.core.{FSContext, FSFileUpload, FSUploadedFile}
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.utils.IdGen

import scala.util.Try
import scala.xml.{Elem, NodeSeq}

class F7FileUploadField()(implicit renderer: F7FileUploadFieldRenderer)
  extends F7FieldWithValue[Seq[F7UploadedFile]]
    with F7FieldWithoutChildren
    with F7FieldWithMainElem
    with F7FieldWithMainElemWithValidation
    with F7FieldWithFileUploadSettings
    with F7FieldFocusableMainElem
    with F7FieldWithDisabled
    with F7FieldWithRequired
    with F7FieldWithReadOnly
    with F7FieldWithEnabled
    with F7FieldWithTabIndex
    with F7FieldWithName
    with F7FieldWithLabel
    with F7FieldWithMainElemId
    with F7FieldWithValidFeedback
    with F7FieldWithHelp
    with F7FieldWithOnChangedField
    with F7FieldWithSyncToServerOnChange
    with F7FieldWithAdditionalAttrs
    with F7FieldWithDependencies {

  lazy val progressBarElemId = "progress_bar_elem_" + IdGen.id
  lazy val progressElemId = "progress_elem_" + IdGen.id

  override def defaultValue: Seq[F7UploadedFile] = Nil

  override def validate(): Seq[(F7Field, NodeSeq)] = super.validate() ++
    (if (required && currentValue.isEmpty) Seq((this, scala.xml.Text(renderer.defaultRequiredFieldLabel))) else Seq())

  override def submit()(implicit form: Form7, fsc: FSContext): Js = super.submit() & _setter(currentValue)

  override def updateFieldValueWithoutReRendering(previous: Seq[F7UploadedFile], current: Seq[F7UploadedFile])(implicit form: Form7, fsc: FSContext): Try[Js] = ???

  def processUpload(files: Seq[FSUploadedFile])(implicit form: Form7, fsc: FSContext): Js = {
    setFilled()
    currentValue = files.map(f => F7UploadedFile(f.name, f.contentType, f.bytes()))
    _renderedValue.setRendered()
    form.onEvent(ChangedField(this))
  }

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  override protected def renderImpl()(implicit form: Form7, fsc: FSContext): Elem = {
    val errorsToShow: Seq[(F7Field, NodeSeq)] = if (shouldShowValidation_?) validate() else Nil
    showingValidation = errorsToShow.nonEmpty

    val field = this

    val previewRerendererP = JS.rerenderableP[FSContext => Elem](_ => fsc => renderer => renderer(fsc))

    val actionUrl = fsc.fileUploadActionUrl({
      case uploadedFile => processUpload(uploadedFile) & previewRerendererP.rerender(fsc => field.previewRenderer(currentValue)(fsc))
    })

    val doUpload = Js(
      s"""
         |var fileInput = document.getElementById(${JS.asJsStr(mainElemId)});
         |var formdata = new FormData();
         |for (var i = 0; i < fileInput.files.length; i++) {
         |    formdata.append('file', fileInput.files[i]);
         |}
         |var request = new XMLHttpRequest();
         |request.upload.addEventListener('progress', function (e) {
         |    if (e.lengthComputable) {
         |        var percent = Math.round((e.loaded / e.total) * 100);
         |
         |        var progressBar = document.getElementById(${JS.asJsStr(progressBarElemId)});
         |        progressBar.style.display = "block";
         |        progressBar.style.width = percent + '%';
         |        progressBar.innerHTML = percent + '%';
         |    }
         |});
         |request.onload = function() { try {eval(this.responseText);} catch(err) { console.log(err.message); console.log('While runnning the code:\\n' + this.responseText); } };
         |
         |request.open('post', ${JS.asJsStr(actionUrl)});
         |request.timeout = $uploadTimeoutMillis;
         |request.send(formdata);
         |""".stripMargin
    )

    val inputElem =
        <input
          name="file"
          type="file"
          accept={acceptTypes.getOrElse(null)}
          multiple={Some("true").filter(_ => multiple).getOrElse(null)}
          id={mainElemId}
          onchange={doUpload.cmd}
        />

    val progressBarElem = progressElemTransforms(renderer.renderProgress(progressBarElemTransforms(renderer.renderProgressBar().withId(progressBarElemId))).withId(progressElemId).withStyle("display:none;"))

    renderer.render(this)(
      inputElem = inputElem,
      previewRerenderer = previewRerendererP,
      progressElem = progressBarElem,
      label = this.label,
      invalidFeedback = errorsToShow.headOption.map(error => <div>{error._2}</div>),
      validFeedback = if (errorsToShow.isEmpty) validFeedback else None,
      help = help
    )
  }
}
