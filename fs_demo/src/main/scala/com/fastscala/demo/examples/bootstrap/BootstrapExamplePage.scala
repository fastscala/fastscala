package com.fastscala.demo.examples.bootstrap

import com.fastscala.core.FSContext
import com.fastscala.demo.examples.MultipleCodeExamplesPage
import scala.xml.NodeSeq

class BootstrapExamplePage extends MultipleCodeExamplesPage("/com/fastscala/demo/examples/bootstrap/BootstrapExamplePage.scala") {

  override def pageTitle: String = "Bootstrap examples"

  override def renderStandardPageContents()(implicit fsc: FSContext): NodeSeq = {
    codeSnippet(file, "=== code snippet #1 ===") ++ {
      // === code snippet #1 ===
      import com.fastscala.templates.bootstrap5.classes.BSHelpers._
      div.apply("hello")
      // === code snippet #1 ===
    }
  }
}
