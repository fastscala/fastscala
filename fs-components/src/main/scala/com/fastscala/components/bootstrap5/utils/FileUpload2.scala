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

object FileUpload2 {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  def apply(
             processUpload: Seq[FSUploadedFile] => Js,
             labelOpt: Option[Elem] = None,
             transformSubmit: Elem => Elem = (_: Elem).apply("Upload").btn.btn_success.mt_2.w_100,
             transformProgress: Elem => Elem = (_: Elem).withStyle("height: 20px;").mt_2,
             transformProgressBar: Elem => Elem = identity[Elem],
             buttonLbl: Option[String] = None,
             multiple: Boolean = false,
             clipboardUpload: Boolean = false,
             acceptTypes: Option[String] = None,
             inputClasses: String = "form-control"
           )(implicit fsc: FSContext): NodeSeq = {
    import com.fastscala.core.circe.CirceSupport.*

    val actionUrl = fsc.fileUploadActionUrl({
      case uploadedFile => processUpload(uploadedFile)
    })
    val progressId = IdGen.id("progress")
    val progressBarId = IdGen.id("progressBar")
    val inputId = IdGen.id("input")
    val buttonId = IdGen.id("btn")
    ScalaXmlElemUtils.showIf(clipboardUpload) {
      val callback = fsc.callbackJSON(JS("[fileName, fileType, base64String]"), json => {
        json.arrayOrObject(
          JS.void,
          {
            case Vector(fileName, fileType, contentsEncoded) =>
              val bytes = Base64.getDecoder().decode(contentsEncoded.asString.get)
              processUpload(
                Seq(new FSUploadedFile(
                  fileName.asString.get,
                  fileName.asString.get,
                  fileType.asString.get,
                  () => bytes,
                  () => new ByteArrayInputStream(bytes)
                ))
              )
          },
          obj => JS.void
        )
      })
      JS.inScriptTag(JS(
        s"""document.addEventListener('paste', function (evt) {
           |    const items = evt.clipboardData.items;
           |    if (items.length === 0) { return; }
           |    const item = items[0];
           |    const blob = item.getAsFile();
           |    const fileName = blob.name;
           |    const fileType = blob.type;
           |	  const reader = new FileReader();
           |	  reader.onloadend = () => {
           |	  	const base64String = reader.result
           |		  	.replace('data:', '')
           |		  	.replace(/^.+,/, '');
           |     $callback
           |	};
           |	reader.readAsDataURL(blob);
           |});
           |""".stripMargin
      ).onDOMContentLoaded)
    } ++
      <form>
        {
        labelOpt.map(label => label.withFor(inputId)).getOrElse(Empty)
        }
        <input class={inputClasses} name="file" type="file" accept={acceptTypes.getOrElse(null)} multiple={Some("true").filter(_ => multiple).getOrElse(null)} id={inputId} onchange={JS.show(buttonId).cmd} />
        {
        transformProgress(<div class="progress" role="progressbar">{transformProgressBar(<div class="progress-bar progress-bar-striped progress-bar-animated"></div>.withId(progressBarId))}</div>.withId(progressId).withStyle("display:none;"))
        }
        {
        transformSubmit(
          BSBtn().BtnPrimary.sm.id(buttonId).withStyle("display:none").lbl(buttonLbl.getOrElse("")).onclick(JS.hide(buttonId) & JS.show(progressId) & Js(
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
               |        // progressBar.innerHTML = percent + '%';
               |    }
               |});
               |
               |request.open('post', ${JS.asJsStr(actionUrl)});
               |request.timeout = 600000;
               |request.send(formdata);
               |""".stripMargin
          )).btn
        )
        }
      </form>
  }

  def withZipSupport(
                      callback: List[(String, Array[Byte])] => Js,
                      labelOpt: Option[Elem] = None,
                      transformSubmit: Elem => Elem = (_: Elem).apply("Upload").btn.btn_success.mt_2.w_100,
                      buttonLbl: Option[String] = None,
                      multiple: Boolean = false
                    )(implicit fsc: FSContext): NodeSeq = apply(uploadedFiles =>

    callback(uploadedFiles.flatMap(uploadedFile => {
      if (uploadedFile.name.trim.toLowerCase.endsWith(".zip")) {
        val zipFile = new ZipInputStream(new ByteArrayInputStream(uploadedFile.bytes()))

        Iterator.continually(zipFile.getNextEntry).takeWhile(_ != null).map(entry => {
          (entry.getName, Iterator.continually(zipFile.read()).takeWhile(_ >= 0).map(_.toByte).toArray[Byte])
        }).toList
      } else {
        List((uploadedFile.name, uploadedFile.bytes()))
      }
    }).toList)
    , labelOpt = labelOpt
    , transformSubmit = transformSubmit
    , buttonLbl = buttonLbl
    , multiple = multiple
  )
}
