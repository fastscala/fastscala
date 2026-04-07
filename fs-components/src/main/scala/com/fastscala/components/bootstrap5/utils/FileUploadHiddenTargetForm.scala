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

object FileUploadHiddenTargetForm {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  def apply(
             processUpload: Seq[FSUploadedFile] => Js,
             labelOpt: Option[Elem] = None,
             transformSubmit: Elem => Elem = (_: Elem).apply("Upload").btn.btn_success.mt_2.w_100,
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
    val targetId = IdGen.id("targetFrame")
    val inputId = IdGen.id("input")
    val buttonId = IdGen.id("btn")
    (ScalaXmlElemUtils.showIf(clipboardUpload)(FileUpload.pasteFromClipboard(processUpload)) ++
      <iframe id={targetId} name={targetId} src="about:blank" onload="eval(this.contentWindow.document.body.innerText)" style="width:0;height:0;border:0px solid #fff;"><html><body></body></html></iframe> ++
      <form target={targetId} action={actionUrl} method="post" encoding="multipart/form-data" enctype="multipart/form-data" >
        {
        labelOpt.map(label => label.withFor(inputId)).getOrElse(Empty)
        }
        <input class={inputClasses} name="file" type="file" accept={acceptTypes.getOrElse(null)} multiple={Some("true").filter(_ => multiple).getOrElse(null)} id={inputId} onchange={JS.show(buttonId).cmd} />
        {
        transformSubmit(button.withId(buttonId).withStyle("display:none").withTypeSubmit()).pipe(btn => buttonLbl.map(lbl => btn.apply(lbl)).getOrElse(btn))
        }
      </form>)
  }
}
