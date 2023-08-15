package com.fastscala.templates.bootstrap5.examples.bootstrap

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.examples.MultipleCodeExamplesPage
import com.fastscala.templates.bootstrap5.utils.BSBtn

import scala.xml.NodeSeq

class BootstrapTypographyPage extends MultipleCodeExamplesPage("/com/fastscala/templates/bootstrap5/examples/bootstrap/BootstrapTypographyPage.scala") {

  override def pageTitle: String = "Bootstrap examples"

  override def renderStandardPageContents()(implicit fsc: FSContext): NodeSeq = {

    import com.fastscala.templates.bootstrap5.classes.BSHelpers._

    display_6.apply("Lead") ++
      codeSnippet(file, "=== code snippet #1 ===") ++ div.apply {
      // === code snippet #1 ===
      p.lead.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut viverra nulla eget maximus posuere. Etiam tristique tincidunt maximus. Proin volutpat, ante non ornare scelerisque, massa diam lobortis turpis, et vehicula felis lorem et nulla.")
      // === code snippet #1 ===
    }.mb_3 ++
      display_6.apply("Inline text elements") ++
      codeSnippet(file, "=== code snippet #2 ===") ++ div.apply {
      // === code snippet #2 ===
      p.apply("Lorem ipsum dolor sit amet") ++
        p.apply(del.apply("Lorem ipsum dolor sit amet")) ++
        p.apply(s.apply("Lorem ipsum dolor sit amet")) ++
        p.apply(ins.apply("Lorem ipsum dolor sit amet")) ++
        p.apply(u.apply("Lorem ipsum dolor sit amet")) ++
        p.apply(small.apply("Lorem ipsum dolor sit amet")) ++
        p.apply(strong.apply("Lorem ipsum dolor sit amet")) ++
        p.apply(em.apply("Lorem ipsum dolor sit amet")) ++
        p.apply(mark.apply("Lorem ipsum dolor sit amet"))
      // === code snippet #2 ===
    }.mb_3 ++
      display_6.apply("Abbreviations") ++
      codeSnippet(file, "=== code snippet #3 ===") ++ div.apply {
      // === code snippet #3 ===
      p.apply(abbr.withAttr("title" -> "title contents").apply("Lorem ipsum dolor sit amet"))
      // === code snippet #3 ===
    }.mb_3 ++
      display_6.apply("Blockquotes") ++
      codeSnippet(file, "=== code snippet #4 ===") ++ div.apply {
      // === code snippet #4 ===
      blockquote.blockquote.apply(p.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit."))
      // === code snippet #4 ===
    }.mb_3
  }
}
