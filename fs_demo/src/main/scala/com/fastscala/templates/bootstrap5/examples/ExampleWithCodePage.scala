package com.fastscala.templates.bootstrap5.examples

import com.fastscala.core.FSContext

import scala.xml.NodeSeq


abstract class ExampleWithCodePage(file: String) extends PageWithTopTitle {

  def renderExampleContents()(implicit fsc: FSContext): NodeSeq

  override def renderStandardPageContents()(implicit fsc: FSContext): NodeSeq = {
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    row.apply {
      col_xl_6.mb_3.apply {
        codeSnippet(file)
      } ++ col_xl_6.apply {
        renderExampleContents()
      }
    }
  }
  // === code snippet ===
}
