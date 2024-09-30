package com.fastscala.demo.docs.fastscala

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.SingleCodeExamplePage
import com.fastscala.templates.bootstrap5.utils.FileUpload
import com.fastscala.xml.scala_xml.FSScalaXmlSupport.fsXmlSupport
import com.fastscala.xml.scala_xml.{FSScalaXmlEnv, JS}

import scala.xml.NodeSeq

class AnonymousPage() extends SingleCodeExamplePage() {

  override def pageTitle: String = "Anonymous Page"

  override def renderExampleContents()(implicit fsc: FSContext): NodeSeq = {
    // === code snippet ===
    import com.fastscala.templates.bootstrap5.helpers.BSHelpers._
    JS.rerenderable(rerenderer => implicit fsc => {
      div.border.p_2.rounded.apply {
        h3.apply("Upload an image:") ++
          FileUpload(uploadedFile => {
            rerenderer.rerender()
            JS.redirectTo(fsc.anonymousPageURL[FSScalaXmlEnv.type](implicit fsc => {
              new VisualizeUploadedImageAnonymousPage(uploadedFile.head.contentType, uploadedFile.head.content).render()
            }, "visualize_image"))
          })
      }
    }).render()
    // === code snippet ===
  }
}


