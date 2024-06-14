package com.fastscala.demo.docs.js

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.js.Js

import java.util.UUID

class JsUtilsPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Js utils"

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {

    import com.fastscala.templates.bootstrap5.classes.BSHelpers._

    renderSnippet("Create Js") {
      button.btn.btn_success.apply("click me").addOnClick(Js("alert('clicked')"))
    }
    renderSnippet("Escape string") {
      val string = """string with 'single' and "double" quotes"""
      button.btn.btn_success.apply("click me").addOnClick(Js(s"""alert("${Js.escapeStr(string)}")""")) ++
        button.ms_2.btn.btn_primary.apply("click me").addOnClick(Js(s"""alert('${Js.escapeStr(string)}')"""))
    }
    renderSnippet("Element by id") {
      d_flex.justify_content_between.apply {
        input.withType("text").withId("id1").withValue("Lorem ipsum dolor sit amet").form_control ++
          button.ms_2.btn.btn_primary.apply("Get input elem").addOnClick(Js(s"""alert('Elem: '+${Js.elementById("id1")})""")).text_nowrap
      }
    }
    renderSnippet("Element value by id") {
      d_flex.justify_content_between.apply {
        input.withType("text").withId("id2").withValue("All work and no play makes Jack a dull boy").form_control ++
          button.ms_2.btn.btn_primary.apply("Get input elem value").addOnClick(Js(s"""alert('Elem: '+${Js.elementValueById("id2")})""")).text_nowrap
      }
    }
    renderSnippet("Copy to clipboard") {
      button.btn.btn_success.apply("Copy UUID").addOnClick(Js.toClipboard(UUID.randomUUID().toString))
    }
    closeSnippet()
  }
}
