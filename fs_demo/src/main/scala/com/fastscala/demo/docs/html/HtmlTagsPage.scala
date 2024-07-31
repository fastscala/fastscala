package com.fastscala.demo.docs.html

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

class HtmlTagsPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "HTML tags"

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {

    renderSnippet("Creating div tags") {
      import com.fastscala.templates.bootstrap5.classes.BSHelpers._
      div.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
    }
    renderSnippet("Creating span tags") {
      import com.fastscala.templates.bootstrap5.classes.BSHelpers._
      span.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
    }
    renderSnippet("Creating pre tags") {
      import com.fastscala.templates.bootstrap5.classes.BSHelpers._
      pre.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
    }
    renderSnippet("Creating s tags") {
      import com.fastscala.templates.bootstrap5.classes.BSHelpers._
      s.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
    }
    renderSnippet("Creating u tags") {
      import com.fastscala.templates.bootstrap5.classes.BSHelpers._
      u.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
    }
    renderSnippet("Creating small tags") {
      import com.fastscala.templates.bootstrap5.classes.BSHelpers._
      small.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
    }
    renderSnippet("Creating strong tags") {
      import com.fastscala.templates.bootstrap5.classes.BSHelpers._
      strong.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
    }
    renderSnippet("Creating em tags") {
      import com.fastscala.templates.bootstrap5.classes.BSHelpers._
      em.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
    }
    renderSnippet("Creating b tags") {
      import com.fastscala.templates.bootstrap5.classes.BSHelpers._
      b.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
    }
    renderSnippet("Creating p tags") {
      import com.fastscala.templates.bootstrap5.classes.BSHelpers._
      p.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
    }
    renderSnippet("Creating h1 tags") {
      import com.fastscala.templates.bootstrap5.classes.BSHelpers._
      h1.apply("h1 element")
    }
    renderSnippet("Creating h2 tags") {
      import com.fastscala.templates.bootstrap5.classes.BSHelpers._
      h2.apply("h2 element")
    }
    renderSnippet("Creating h3 tags") {
      import com.fastscala.templates.bootstrap5.classes.BSHelpers._
      h3.apply("h3 element")
    }
    renderSnippet("Creating h4 tags") {
      import com.fastscala.templates.bootstrap5.classes.BSHelpers._
      h4.apply("h4 element")
    }
    renderSnippet("Creating h5 tags") {
      import com.fastscala.templates.bootstrap5.classes.BSHelpers._
      h5.apply("h5 element")
    }
    renderSnippet("Creating h6 tags") {
      import com.fastscala.templates.bootstrap5.classes.BSHelpers._
      h6.apply("h6 element")
    }
    renderSnippet("Creating hr tags") {
      import com.fastscala.templates.bootstrap5.classes.BSHelpers._
      hr
    }
    closeSnippet()
  }
}
