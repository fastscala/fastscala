package com.fastscala.demo.docs.fastscala

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.bootstrap5.utils.FileUpload
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

class AnonymousPage() extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Anonymous Page"

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderSnippet("Source") {
      import com.fastscala.components.bootstrap5.helpers.BSHelpers.*
      JS.rerenderable(rerenderer => implicit fsc => {
        div.border.p_2.rounded.apply {
          h3.apply("Upload an image:") ++
            FileUpload(uploadedFile => {
              rerenderer.rerender()
              JS.redirectTo(fsc.anonymousPageURL(implicit fsc => {
                new VisualizeUploadedImageAnonymousPage(uploadedFile.head.contentType, uploadedFile.head.content).render().toString
              }, "visualize_image"))
            })
        }
      }).render()
    }
    closeSnippet()
  }
}


