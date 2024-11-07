package com.fastscala.demo.docs

import com.fastscala.core.FSContext

import scala.xml.NodeSeq

class EmptyPage extends BasePage {

  override def pageTitle: String = "FastScala | Empty page"

  override def renderPageContents()(implicit fsc: FSContext): NodeSeq = {
    <p>Empty page for you to do your testing 😁</p>
  }
}
