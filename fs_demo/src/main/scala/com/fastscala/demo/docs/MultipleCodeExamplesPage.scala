package com.fastscala.demo.docs

import com.fastscala.demo.db.User
import org.eclipse.jetty.util.IO

import java.nio.charset.StandardCharsets
import java.nio.file.Path

import scala.util.matching.Regex
import scala.xml.NodeSeq

abstract class MultipleCodeExamplesPage(val file: String)(implicit val user: User) extends PageWithTopTitle {
  override def append2Head(): NodeSeq = super.append2Head() ++
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/styles/atom-one-light.min.css" integrity="sha512-o5v54Kh5PH0dgnf9ei0L+vMRsbm5fvIvnR/XkrZZjN4mqdaeH7PW66tumBoQVIaKNVrLCZiBEfHzRY4JJSMK/Q==" crossorigin="anonymous" referrerpolicy="no-referrer" />

  override def append2Body(): NodeSeq = super.append2Body() ++ {
    <script src="//cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/highlight.min.js"></script>
    <script src="//cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/languages/scala.min.js"></script>
    <script>hljs.highlightAll();</script>
  }

  def codeSnippet(file: String, separator: String = "=== code snippet ==="): NodeSeq = {
    val allCode = IO.toString(Path.of(getClass.getResource(file).toURI()), StandardCharsets.UTF_8)
    val allSections: Array[String] = allCode.split("\n.*" + Regex.quote(separator) + ".*\n")
    val relevantSections: List[String] = allSections.zipWithIndex.toList.collect({
      case (code, idx) if (idx + 1) % 2 == 0 => code.replaceAll("(^|\n).*" + Regex.quote(separator) + ".*\n", "")
    })
    val code = relevantSections.mkString("\n\n// [...]\n\n")
    val leftPadding: Int = code.split("\n").iterator.map(_.takeWhile(_ == ' ').size).filter(_ > 0).minOption.getOrElse(0)
    val withoutPadding = code.split("\n").map(_.drop(leftPadding)).mkString("\n")

    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    div.apply {
      <pre><code style="background-color: #eee;" class="language-scala">{withoutPadding}</code></pre>.m_0
    }
  }
}
