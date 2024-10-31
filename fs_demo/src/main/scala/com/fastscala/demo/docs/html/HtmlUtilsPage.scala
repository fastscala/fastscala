package com.fastscala.demo.docs.html

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.js.Js

import scala.xml.NodeSeq

class HtmlUtilsPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "HTML Basics"

  import com.fastscala.templates.bootstrap5.helpers.BSHelpers._

  override def renderExplanation()(implicit fsc: FSContext): NodeSeq =
    <p>
      Remember you need the import: {span.apply("import com.fastscala.templates.bootstrap5.helpers.BSHelpers.").text_bg_primary.d_inline}
    </p>

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderSnippet("Adding a class") {
      span.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit.").withClass("text-bg-success")
    }
    renderSnippet("Adding a style") {
      span.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit.").withStyle("color: green;")
    }
    renderSnippet("Adding an onclick handler") {
      span.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit.").addOnClick(Js.alert("Clicked"))
    }
    renderSnippet("Adding a class conditionally") {
      val value = math.random() - 0.5
      span.apply(f"$value%.2f").withClassIf(value < 0, "text-bg-danger")
    }
    renderSnippet("Adding an attribute conditionally") {
      val value = math.random() - 0.5
      span.apply(f"$value%.2f").withAttrIf(value < 0, "style" -> "color: red;")
    }
    renderSnippet("Setting type") {
      input.withType("color")
    }
    renderSnippet("Setting title") {
      span.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit.").withTitle("Title")
    }
    renderSnippet("Setting attribute") {
      input.withType("text").withAttr("placeholder" -> "First name")
    }
    renderSnippet("Setting id") {
      span.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit.").withId("main_label")
    }
    renderSnippet("Setting id if not set already") {
      span.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit.").withIdIfNotSet("main_label")
    }
    renderSnippet("Setting href") {
      a.apply("open google").withHref("https://www.google.com")
    }
    renderSnippet("Prepend/Append to contents") {
      p.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
        .withPrependedToContents(badge.bg_success.apply("prepended"))
        .withAppendedToContents(badge.bg_info.apply("appended"))
    }
    closeSnippet()
  }
}
