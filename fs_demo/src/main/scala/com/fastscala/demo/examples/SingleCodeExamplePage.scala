package com.fastscala.demo.examples

import com.fastscala.core.FSContext
import org.apache.commons.io.IOUtils

import java.nio.charset.StandardCharsets
import scala.util.matching.Regex
import scala.xml.NodeSeq


abstract class SingleCodeExamplePage(file: String) extends PageWithTopTitle {

  override def append2Head(): NodeSeq = super.append2Head() ++
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/styles/vs.min.css" integrity="sha512-AVoZ71dJLtHRlsgWwujPT1hk2zxtFWsPlpTPCc/1g0WgpbmlzkqlDFduAvnOV4JJWKUquPc1ZyMc5eq4fRnKOQ==" crossorigin="anonymous" referrerpolicy="no-referrer" />

  override def append2Body(): NodeSeq = super.append2Body() ++ {
    <script src="//cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/highlight.min.js"></script>
    <script src="//cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/languages/scala.min.js"></script>
    <script>hljs.highlightAll();</script>
  }

  def codeSnippet(file: String, separator: String = "=== code snippet ==="): NodeSeq = {
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    val allCode = IOUtils.resourceToString(file, StandardCharsets.UTF_8)
    val codeSections: List[String] = allCode.split("\n.*" + Regex.quote(separator) + ".*\n").zipWithIndex.toList.collect({
      case (code, idx) if (idx + 1) % 2 == 0 => code
    })
    div.border.border_secondary.rounded.apply {
      h3.apply("Source Code").bg_secondary.text_white.px_3.py_2.m_0.border_bottom.border_secondary ++
        div.apply {
          <pre><code style="background-color: #eee;" class="language-scala">{codeSections.mkString("\n\n// [...]\n\n")}</code></pre>.m_0
        }
    }
  }

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
}
