package com.fastscala.demo.docs

import com.fastscala.core.FSContext
import com.fastscala.xml.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromNodeSeq
import org.eclipse.jetty.util.{IO, VirtualThreads}

import java.nio.charset.StandardCharsets
import java.nio.file.Path

import scala.xml.NodeSeq

abstract class MultipleCodeExamples2Page() extends LoggedInPage() {

  def file = getClass.getName.split("\\.").mkString("/", "/", ".scala")

  override def append2Head(): NodeSeq = super.append2Head() ++
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/styles/atom-one-light.min.css" integrity="sha512-o5v54Kh5PH0dgnf9ei0L+vMRsbm5fvIvnR/XkrZZjN4mqdaeH7PW66tumBoQVIaKNVrLCZiBEfHzRY4JJSMK/Q==" crossorigin="anonymous" referrerpolicy="no-referrer" />

  override def append2Body(): NodeSeq = super.append2Body() ++ {
    <script src="//cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/highlight.min.js"></script>
    <script src="//cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/languages/scala.min.js"></script>
    <script>hljs.highlightAll();</script>
  }

  def pageTitle: String

  def renderExplanation()(implicit fsc: FSContext): NodeSeq = NodeSeq.Empty

  def renderContentsWithSnippets()(implicit fsc: FSContext): Unit

  override def renderPageContents()(implicit fsc: FSContext): NodeSeq = {
    import com.fastscala.templates.bootstrap5.helpers.BSHelpers._
    div.withStyle("background('#f8fafd'); border-style: solid; border-color: #b3c7de;").border_1.shadow_sm.py_2.px_3.apply {
      <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-3 mb-3">
        <h1 class="h3" style="color: #1b4d88;">{pageTitle}</h1>
      </div> ++
        renderStandardPageContents()
    }
  }

  def renderStandardPageContents()(implicit fsc: FSContext): NodeSeq = {
    renderContentsWithSnippets()
    renderExplanation() ++
      sections.reverse.mkNS
  }

  def renderCodeSnippet(title: String, rendered: NodeSeq, contents: NodeSeq): NodeSeq = {
    import com.fastscala.templates.bootstrap5.helpers.BSHelpers._
    h4.pb_1.border_bottom.border_secondary_subtle.apply(title) ++
      div.row.apply {
        div.col_md_6.mb_2.apply {
          div.border.border_secondary_subtle.bg_white.apply {
            div.apply {
              rendered
            }
          }
        } ++ div.col_md_6.mb_2.apply {
          div.border.border_secondary_subtle.bg_white.apply {
            div.p_3.apply {
              contents
            }
          }
        }
      }
  }

  var lastSection: Option[(Int, String, NodeSeq)] = None
  var sections: List[NodeSeq] = Nil

  val lines = IO.toString(Path.of(getClass.getResource(file).toURI()), StandardCharsets.UTF_8).split("\\n")
  val stackTracePos = if (VirtualThreads.areSupported) 2 else 3

  def renderSnippet(
                     title: String,
                     thisSectionStartsAt: Int = Thread.currentThread.getStackTrace.apply(stackTracePos).getLineNumber
                   )(contents: => NodeSeq): Unit = {
    collectSection(thisSectionStartsAt)
    lastSection = Some((thisSectionStartsAt, title, contents))
  }

  def renderHtml(
                  thisSectionStartsAt: Int = Thread.currentThread.getStackTrace.apply(stackTracePos).getLineNumber
                )(contents: => NodeSeq): Unit = {
    collectSection(thisSectionStartsAt)
    lastSection = None
    sections ::= contents
  }

  def collectSection(thisSectionStartsAt: Int): Unit = {
    import com.fastscala.templates.bootstrap5.helpers.BSHelpers._
    lastSection.foreach({
      case (lastSectionStartedAt, title, contents) =>
        val code = lines.drop(lastSectionStartedAt).take(thisSectionStartsAt - lastSectionStartedAt - 2)
        println(s"$lastSectionStartedAt:$thisSectionStartsAt: \n${code.mkString("\n")}")
        val leftPadding: Int = code.iterator.map(_.takeWhile(_ == ' ').size).filter(_ > 0).minOption.getOrElse(0)
        val withoutPadding = code.map(_.drop(leftPadding)).mkString("\n")
        val rendered = div.apply {
          <pre><code style="background-color: #eee;" class="language-scala">{withoutPadding}</code></pre>.m_0
        }
        sections ::= renderCodeSnippet(title, rendered, contents)
    })
  }

  def closeSnippet(): Unit = renderSnippet("", Thread.currentThread.getStackTrace.apply(stackTracePos).getLineNumber)(NodeSeq.Empty)
}
