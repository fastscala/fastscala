package com.fastscala.components.bootstrap5.utils

import com.fastscala.core.{FSContext, FSUploadedFile}
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils
import com.fastscala.scala_xml.js.JS
import com.fastscala.utils.IdGen
import ScalaXmlElemUtils.RichElem

import java.io.ByteArrayInputStream
import java.util.Base64
import java.util.zip.ZipInputStream
import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

object FileUpload {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  def apply(
             processUpload: Seq[FSUploadedFile] => Js,
             labelOpt: Option[Elem] = None,
             transformSubmit: Elem => Elem = (_: Elem).apply("Upload").btn.btn_success.mt_2.w_100,
             buttonLbl: Option[String] = None,
             multiple: Boolean = false,
             clipboardUpload: Boolean = false,
             acceptTypes: Option[String] = None
           )(implicit fsc: FSContext): NodeSeq = {
    import com.fastscala.core.circe.CirceSupport.*

    val actionUrl = fsc.fileUploadActionUrl({
      case uploadedFile => processUpload(uploadedFile)
    })
    val targetId = IdGen.id("targetFrame")
    val inputId = IdGen.id("input")
    val buttonId = IdGen.id("btn")
    (ScalaXmlElemUtils.showIf(clipboardUpload) {
      val callback = fsc.callbackJSON(JS("[fileName, fileType, base64String]"), json => {
        json.arrayOrObject(
          JS.void,
          {
            case Vector(fileName, fileType, contentsEncoded) =>
              processUpload(
                Seq(new FSUploadedFile(
                  fileName.asString.get,
                  fileName.asString.get,
                  fileType.asString.get,
                  Base64.getDecoder().decode(contentsEncoded.asString.get)
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
      <iframe id={targetId} name={targetId} src="about:blank" onload="eval(this.contentWindow.document.body.innerText)" style="width:0;height:0;border:0px solid #fff;"><html><body></body></html></iframe> ++
      <form target={targetId} action={actionUrl} method="post" encoding="multipart/form-data" enctype="multipart/form-data" >
        {
        labelOpt.map(label => label.withFor(inputId)).getOrElse(Empty)
        }
        <input class="form-control" name="file" type="file" accept={acceptTypes.getOrElse(null)} multiple={Some("true").filter(_ => multiple).getOrElse(null)} id={inputId} onchange={JS.show(buttonId).cmd} />
        {
        transformSubmit(button.withId(buttonId).withStyle("display:none").withTypeSubmit()).pipe(btn => buttonLbl.map(lbl => btn.apply(lbl)).getOrElse(btn))
        }
      </form>)
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
        val zipFile = new ZipInputStream(new ByteArrayInputStream(uploadedFile.content))

        Iterator.continually(zipFile.getNextEntry).takeWhile(_ != null).map(entry => {
          (entry.getName, Iterator.continually(zipFile.read()).takeWhile(_ >= 0).map(_.toByte).toArray[Byte])
        }).toList
      } else {
        List((uploadedFile.name, uploadedFile.content))
      }
    }).toList)
    , labelOpt = labelOpt
    , transformSubmit = transformSubmit
    , buttonLbl = buttonLbl
    , multiple = multiple
  )
}
