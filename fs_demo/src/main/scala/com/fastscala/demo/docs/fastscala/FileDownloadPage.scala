package com.fastscala.demo.docs.fastscala

import com.fastscala.core.{FSContext, FSSessionVarOpt, FSUploadedFile}
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.templates.bootstrap5.utils.{BSBtn, FileUpload}
import com.fastscala.xml.scala_xml.JS

import java.util.Base64

// === code snippet ===
object FileDownloadPageUploadedImage extends FSSessionVarOpt[FSUploadedFile]()
// === code snippet ===

class FileDownloadPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "File Download"

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderSnippet("Source") {
      import com.fastscala.templates.bootstrap5.helpers.BSHelpers._
      FileDownloadPageUploadedImage.clear()
      JS.rerenderable(rerenderer => implicit fsc => {
        div.border.p_2.rounded.apply {
          FileDownloadPageUploadedImage() match {
            case Some(uploadedFile) =>

              val fileDownloadUrl = fsc.fileDownload(uploadedFile.submittedFileName.replaceAll(".*\\.(\\w+)$", "uploaded.$1"), uploadedFile.contentType, () => uploadedFile.content)

              h3.apply("Uploaded image:") ++
                <img class="w-100" src={s"data:${uploadedFile.contentType};base64, " + Base64.getEncoder.encodeToString(uploadedFile.content)}></img>.mx_auto.my_4.d_block ++
                BSBtn().BtnPrimary.lbl("Download Uploaded File").href(fileDownloadUrl).btnLink.d_block
            case None =>
              h3.apply("Upload an image:") ++
                FileUpload(
                  uploadedFile => {
                    FileDownloadPageUploadedImage() = uploadedFile.head
                    rerenderer.rerender()
                  })
          }
        }
      }).render()
    }
    closeSnippet()
  }
}
