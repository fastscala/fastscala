package com.fastscala.demo.docs.fastscala

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.templates.bootstrap5.utils.FileUpload
import com.fastscala.xml.scala_xml.FSScalaXmlSupport.fsXmlSupport
import com.fastscala.xml.scala_xml.{FSScalaXmlEnv, JS}

class AnonymousPage() extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Anonymous Page"

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderSnippet("Source") {
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
    }
    closeSnippet()
  }
}


