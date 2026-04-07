package com.fastscala.components.bootstrap5.utils

import com.fastscala.core.{FSContext, FSUploadedFile}
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.{JS, inScriptTag}
import com.fastscala.utils.IdGen

import java.io.ByteArrayInputStream
import java.util.Base64
import java.util.zip.ZipInputStream
import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

object FileUploadDedicatedXmlHttpRequest {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  def apply(
             processUpload: Seq[FSUploadedFile] => Js,
             labelOpt: Option[Elem] = None,
             transformSubmitBtn: Elem => Elem = (_: Elem).btn.btn_success.mt_2.w_100,
             transformProgress: Elem => Elem = (_: Elem).withStyle("height: 20px;").mt_2,
             transformProgressBar: Elem => Elem = identity[Elem],
             submitBtnLbl: Option[String] = Some("Upload"),
             multiple: Boolean = false,
             clipboardUploadEnabled: Boolean = false,
             acceptTypes: Option[String] = None,
             inputClasses: String = "form-control",
             uploadTimeoutMillis: Long = 600000
           )(implicit fsc: FSContext): NodeSeq = {
    import com.fastscala.core.circe.CirceSupport.*

    val actionUrl = fsc.fileUploadActionUrl({
      case uploadedFile => processUpload(uploadedFile)
    })
    val progressId = IdGen.id("progress")
    val progressBarId = IdGen.id("progressBar")
    val inputId = IdGen.id("input")
    val buttonId = IdGen.id("btn")
    ScalaXmlElemUtils.showIf(clipboardUploadEnabled)(FileUpload.pasteFromClipboard(processUpload)) ++
      <form>
        {
        labelOpt.map(label => label.withFor(inputId)).getOrElse(Empty)
        }
        <input class={inputClasses} name="file" type="file" accept={acceptTypes.getOrElse(null)} multiple={Some("true").filter(_ => multiple).getOrElse(null)} id={inputId} onchange={JS.show(buttonId).cmd} />
        {
        transformProgress(<div class="progress" role="progressbar">{transformProgressBar(<div class="progress-bar progress-bar-striped progress-bar-animated"></div>.withId(progressBarId))}</div>.withId(progressId).withStyle("display:none;"))
        }
        {
        transformSubmitBtn(
          BSBtn().BtnPrimary.sm.id(buttonId).withStyle("display:none").lbl(submitBtnLbl.getOrElse("")).onclick(JS.hide(buttonId) & JS.show(progressId) & Js(
            s"""
               |var fileInput = document.getElementById(${JS.asJsStr(inputId)});
               |var formdata = new FormData();
               |for (var i = 0; i < fileInput.files.length; i++) {
               |    formdata.append('file', fileInput.files[i]);
               |}
               |var request = new XMLHttpRequest();
               |request.upload.addEventListener('progress', function (e) {
               |    if (e.lengthComputable) {
               |        var percent = Math.round((e.loaded / e.total) * 100);
               |
               |        var progressBar = document.getElementById(${JS.asJsStr(progressBarId)});
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
          )).btn
        )
        }
      </form>
  }
}
