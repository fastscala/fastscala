package com.fastscala.demo.docs.fastscala

import com.fastscala.core.{FSContext, FSSessionVarOpt, FSUploadedFile}
import com.fastscala.demo.docs.SingleCodeExamplePage
import com.fastscala.templates.bootstrap5.utils.FileUpload
import com.fastscala.xml.scala_xml.JS

import java.util.Base64
import scala.xml.NodeSeq

// === code snippet ===
object UploadedImage extends FSSessionVarOpt[FSUploadedFile]()
// === code snippet ===

class FileUploadPage extends SingleCodeExamplePage() {

  override def pageTitle: String = "File Upload Example"

  override def renderExampleContents()(implicit fsc: FSContext): NodeSeq = {
    // === code snippet ===
    import com.fastscala.templates.bootstrap5.helpers.BSHelpers._
    JS.rerenderable(rerenderer => implicit fsc => {
      div.border.p_2.rounded.apply {
        UploadedImage() match {
          case Some(uploadedFile) =>
            h3.apply("Uploaded image:") ++
              <img class="w-100" src={s"data:${uploadedFile.contentType};base64, " + Base64.getEncoder.encodeToString(uploadedFile.content)}></img>.mx_auto.my_4.d_block
          case None =>
            h3.apply("Upload an image:") ++
              FileUpload(
                uploadedFile => {
                  UploadedImage() = uploadedFile.head
                  rerenderer.rerender()
                })
        }
      }
    }).render()
    // === code snippet ===
  }
}
