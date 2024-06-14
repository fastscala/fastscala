package com.fastscala.demo.docs.fastscala

import com.fastscala.core.FSContext
import com.fastscala.demo.db.User
import com.fastscala.demo.docs.SingleCodeExamplePage
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.utils.FileUpload

import java.util.Base64
import scala.xml.NodeSeq

class AnonymousPage() extends SingleCodeExamplePage() {

  override def pageTitle: String = "Anonymous Page"

  override def renderExampleContents()(implicit fsc: FSContext): NodeSeq = {
    // === code snippet ===
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    Js.rerenderable(rerenderer => implicit fsc => {
      div.border.p_2.rounded.apply {
        h3.apply("Upload an image:") ++
          FileUpload(uploadedFile => {
            rerenderer.rerender()
            Js.redirectTo(fsc.anonymousPageURL(implicit fsc => {
              new VisualizeUploadedImageAnonymousPage(uploadedFile.head.contentType, uploadedFile.head.content).render()
            }, "visualize_image"))
          })
      }
    }).render()
    // === code snippet ===
  }
}

// === code snippet ===
class VisualizeUploadedImageAnonymousPage(
                                           contentType: String,
                                           contents: Array[Byte]
                                         ) extends SingleCodeExamplePage() {

  override def pageTitle: String = "Visualize Uploaded Image Anonymous Page Example"

  override def renderExampleContents()(implicit fsc: FSContext): NodeSeq = {
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    Js.rerenderable(rerenderer => implicit fsc => {
      div.border.p_2.rounded.apply {
        h3.apply("Uploaded image:") ++
          <img class="w-100" src={s"data:$contentType;base64, " + Base64.getEncoder.encodeToString(contents)}></img>.mx_auto.my_4.d_block
      }
    }).render()
  }
}
// === code snippet ===

