package com.fastscala.templates.bootstrap5.examples.bootstrap

import com.fastscala.core.FSContext
import com.fastscala.templates.bootstrap5.examples.MultipleCodeExamplesPage
import scala.xml.NodeSeq

class BootstrapExamplePage extends MultipleCodeExamplesPage("/com/fastscala/templates/bootstrap5/examples/bootstrap/BootstrapExamplePage.scala") {

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
