package com.fastscala.demo.docs.bootstrap

import com.fastscala.core.FSContext
import com.fastscala.demo.db.User
import com.fastscala.demo.docs.{MultipleCodeExamples2Page, MultipleCodeExamplesPage}
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.classes.BSHelpers.div
import com.fastscala.templates.bootstrap5.utils.BSBtn

import scala.xml.NodeSeq

class BootstrapBasicsPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Bootstrap Basics"

  override def renderExamples()(implicit fsc: FSContext): Unit = {

    renderSnippet("Basics") {
      import com.fastscala.templates.bootstrap5.classes.BSHelpers._
      div.apply("hello")
    }
    closeSnippet()
  }
}
