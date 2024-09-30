package com.fastscala.demo.docs

import com.fastscala.core.FSContext

import scala.xml.NodeSeq

trait PageWithTopTitle extends LoggedInPage {

  def pageTitle: String

  def pageTitleToolbar()(implicit fsc: FSContext): NodeSeq = NodeSeq.Empty

  def renderStandardPageContents()(implicit fsc: FSContext): NodeSeq

  override def renderPageContents()(implicit fsc: FSContext): NodeSeq = {
    import com.fastscala.templates.bootstrap5.helpers.BSHelpers._
    div.withStyle("background('#f8fafd'); border-style: solid; border-color: #b3c7de;").border_1.shadow_sm.py_2.px_3.apply {
      <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-3 mb-3">
        <h1 class="h3" style="color: #1b4d88;">{pageTitle}</h1>
        <div class="btn-toolbar mb-2 mb-md-0">
          {pageTitleToolbar}
        </div>
      </div> ++
        renderStandardPageContents()
    }
  }
}
