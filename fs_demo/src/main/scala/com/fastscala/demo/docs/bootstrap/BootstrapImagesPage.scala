package com.fastscala.demo.docs.bootstrap

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page

class BootstrapImagesPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Bootstrap Images"

  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

  val image = img.withAttr("src" -> "/static/images/pexels-souvenirpixels-414612.jpg")

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderSnippet("Responsive") {
      image.img_fluid
    }
    renderSnippet("Thumbnails") {
      text_center.apply {
        image.withStyle("max-width: 250px; max-height: 250px;").img_thumbnail
      }
    }
    renderSnippet("Rounded") {
      text_center.apply {
        image.withStyle("max-width: 250px; max-height: 250px;").rounded
      }
    }
    closeSnippet()
  }
}
