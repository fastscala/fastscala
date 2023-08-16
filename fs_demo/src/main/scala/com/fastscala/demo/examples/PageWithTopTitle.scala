package com.fastscala.demo.examples

import com.fastscala.core.FSContext

import scala.xml.NodeSeq


trait PageWithTopTitle extends BasePage {

  def pageTitle: String

  def pageTitleToolbar()(implicit fsc: FSContext): NodeSeq = NodeSeq.Empty

  def renderStandardPageContents()(implicit fsc: FSContext): NodeSeq

  override def renderPageContents()(implicit fsc: FSContext): NodeSeq = {
    <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
      <h1 class="h2">{pageTitle}</h1>
      <div class="btn-toolbar mb-2 mb-md-0">
        {pageTitleToolbar}
      </div>
    </div> ++
      renderStandardPageContents()
  }
}
