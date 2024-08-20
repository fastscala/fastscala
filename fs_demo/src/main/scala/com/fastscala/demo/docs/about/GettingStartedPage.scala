package com.fastscala.demo.docs.about

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.LoggedInPage
import com.fastscala.templates.bootstrap5.utils.BSBtn

import scala.xml.NodeSeq

class GettingStartedPage extends LoggedInPage() {

  override def pageTitle: String = "FastScala Web Framework Author | Getting Started"

  override def renderPageContents()(implicit fsc: FSContext): NodeSeq = {
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    div.withStyle("background('#f8fafd'); border-style: solid; border-color: #b3c7de;").border_1.shadow_sm.py_2.px_3.apply {
      <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-3 mb-3">
        <h1 class="h3" style="color: #1b4d88;">Getting Started</h1>
      </div> ++
        alert.alert_success.withRole("alert").d_flex.justify_content_between.align_items_center.mb_5.apply {
          div.apply("Interested in learning more about the FastScala framework? Register now for a free live demo/training here!:") ++
            BSBtn().BtnPrimary.lbl("Register for Free Training!").href("https://training.fastscala.com/").btnLink.ms_3
        } ++
        h2.apply("Github repo") ++
        <p>You can access the FastScala framework repo at: <a href="https://github.com/fastscala/fastscala">https://github.com/fastscala/fastscala</a></p> ++
        mb_5 ++
        text_center.apply {
          h2.apply("Basic Scala Environment Installation") ++
          <iframe width="560" height="315" src="https://www.youtube.com/embed/DyvwpBI9wms?" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen="true"></iframe>
        } ++
        mb_5 ++
        text_center.apply {
          h2.apply("Running the Demo locally") ++
          <iframe width="560" height="315" src="https://www.youtube.com/embed/9dVYEipQg18?" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen="true"></iframe>
        } ++
        mb_5 ++
        text_center.apply {
          h2.apply("Deploying online in 5min!") ++
          <iframe width="560" height="315" src="https://www.youtube.com/embed/miTc2kx80cs?" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen="true"></iframe>
        }
    }
  }
}
