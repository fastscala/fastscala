package com.fastscala.scala_xml.js

import com.fastscala.core.FSContext
import com.fastscala.js.{Js, JsUtils}
import com.fastscala.scala_xml.rerenderers.{ContentRerenderer, ContentRerendererP, Rerenderer, RerendererP}
import com.fastscala.scala_xml.utils.XmlStream
import com.fastscala.utils.IdGen
import org.apache.commons.text.StringEscapeUtils
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback

import scala.xml.*
import org.owasp.encoder.{Encode, EncodedWriter, Encoders}

import java.io.{OutputStream, OutputStreamWriter}
import java.nio.ByteBuffer

object JS extends JsXmlUtils

class NodeSeqInStringEscapedJs(ns: NodeSeq) extends Js {
  override lazy val lengthUtf8Enc: Long = {
    var length = 0L
    val fake = new OutputStream {

      override def write(i: Int): Unit = length += 1
    }
    val writer = new EncodedWriter(new OutputStreamWriter(fake, "UTF-8"), Encoders.JAVASCRIPT_ATTRIBUTE)
    XmlStream.writeSeq(ns, writer)
    length
    //    Encode.forJavaScript(cmd).getBytes("UTF-8").size
  }

  override def writeUtf8Enc(sink: Content.Sink, callback: Callback): Unit = {
    val writer = new EncodedWriter(new OutputStreamWriter(Content.Sink.asOutputStream(sink), "UTF-8"), Encoders.JAVASCRIPT_ATTRIBUTE)
    XmlStream.writeSeq(ns, writer)
    callback.succeeded()
    //    sink.write(false, ByteBuffer.wrap(Encode.forJavaScript(cmd).getBytes("UTF-8")), callback)
  }

  override def cmd: String = org.owasp.encoder.Encode.forJavaScript(ns.toString())
}

class JsXmlUtils extends JsUtils {

  def rerenderable(render: Rerenderer => FSContext => Elem, idOpt: Option[String] = None, debugLabel: Option[String] = None): Rerenderer =
    new Rerenderer(rerenderer => fsc => render(rerenderer)(fsc), idOpt = idOpt, debugLabel = debugLabel)

  def rerenderableP[P](render: RerendererP[P] => FSContext => P => Elem, idOpt: Option[String] = None, debugLabel: Option[String] = None): RerendererP[P] =
    new RerendererP[P](rerenderer => fsc => param => render(rerenderer)(fsc)(param), idOpt = idOpt, debugLabel = debugLabel)

  def rerenderableContents(render: ContentRerenderer => FSContext => NodeSeq, id: Option[String] = None, debugLabel: Option[String] = None): ContentRerenderer =
    new ContentRerenderer(rerenderer => fsc => render(rerenderer)(fsc), idOpt = id, debugLabel = debugLabel)

  def rerenderableContentsP[P](render: ContentRerendererP[P] => FSContext => P => NodeSeq, id: Option[String] = None, debugLabel: Option[String] = None): ContentRerendererP[P] =
    new ContentRerendererP[P](rerenderer => fsc => param => render(rerenderer)(fsc)(param), idOpt = id, debugLabel = debugLabel)

  def append2BodyWithScriptExtraction(ns: NodeSeq): Js =
    append2Body(excludeJsInScriptTags(ns)) & extractJsInScriptTags(ns)

  def append2Body(ns: NodeSeq): Js = {
    val elemId = IdGen.id("template")
    Js(Js(s"document.body.appendChild("), htmlToElement(ns, elemId), Js(")")) &
      removeId(elemId)
  }

  def append2(id: String, ns: NodeSeq): Js = {
    val elemId = IdGen.id("template")
    Js(Js(s"""document.getElementById("${escapeStr(id)}").appendChild("""), htmlToElement(ns, elemId), Js(""")""")) &
      removeId(elemId)
  }

  def prepend2(id: String, ns: NodeSeq): Js = {
    val elemId = IdGen.id("template")
    Js(Js(s"""document.getElementById("${escapeStr(id)}").insertBefore("""), htmlToElement(ns, elemId), Js(s""", document.getElementById("${escapeStr(id)}").firstChild)""")) &
      removeId(elemId)
  }

  def replaceWithScriptExtraction(id: String, by: NodeSeq): Js =
    replace(id, excludeJsInScriptTags(by)) & extractJsInScriptTags(by)

  def replace(id: String, by: NodeSeq): Js = Js(
    Js(s"""(document.getElementById("${escapeStr(id)}") ? document.getElementById("${escapeStr(id)}").replaceWith("""),
    htmlToElement(by),
    Js(s""") : console.error("Element with id ${escapeStr(id)} not found"));""")
  )

  def setContents(id: String, ns: NodeSeq): Js = Js(
    Js(s"""document.getElementById("${escapeStr(id)}").innerHTML = """"),
    NodeSeqInStringEscapedJs(ns),
    Js("""";"""),
  )

  def setContents(id: String, js: Js): Js = Js(s"""document.getElementById("${escapeStr(id)}").innerHTML = $js; """)

  def htmlToElement(html: NodeSeq, templateId: String = IdGen.id): Js =
    Js(
      Js(s"""(document.body.appendChild((function htmlToElement(html) {var template = document.createElement('template');template.setAttribute("id", "$templateId");template.innerHTML = html.trim(); return template;})(""""),
      NodeSeqInStringEscapedJs(html),
      Js("\"))).content")
    )

  def forceHttps: Elem = {
    <script type="text/javascript">{
      scala.xml.Unparsed(
        """//<![CDATA[
          |if (location.protocol !== 'https:' && location.hostname !== 'localhost') { location.protocol = 'https:'; }
          |//]]>""".stripMargin)
    }</script>
  }
  
  def inScriptTag(js: Js): Elem = {
    <script type="text/javascript">{
      scala.xml.Unparsed(
        """
// <![CDATA[
""" + js +
          """
// ]]>
"""
      )
    }</script>
  }

  def inScriptTagModule(js: Js): Elem = {
    <script type="module">{
      scala.xml.Unparsed(
        """
// <![CDATA[
""" + js +
          """
// ]]>
"""
      )
    }</script>
  }

  def showIf(b: Boolean)(ns: => NodeSeq): NodeSeq = if (b) ns else NodeSeq.Empty

  /**
   * Removes the JS `<script>` tags (see [[isJsScriptTag]]), at any depth, from `ns`.
   * `ns` can be a single node or a sequence of nodes.
   */
  def excludeJsInScriptTags(ns: NodeSeq): NodeSeq = ns match {
    case group: Group => Group(excludeJsInScriptTags(group.nodes: NodeSeq))
    case node: Elem if isJsScriptTag(node) => NodeSeq.Empty
    case node: Elem => node.copy(child = excludeJsInScriptTags(node.child: NodeSeq))
    case node: Node => node // Text, comments, etc.
    case nodes => nodes.flatMap(excludeJsInScriptTags(_))
  }

  private def isJsScriptTag(node: Node): Boolean =
    node.label == "script" &&
      node.attribute("type").exists(_.exists(node => {
        val str = node.toString
        str == "text/javascript" || str == "module"
      }))

  /**
   * The contents of all JS `<script>` tags (see [[isJsScriptTag]]) in `ns`, at any depth, in document order, joined by `;`.
   * `ns` can be a single node or a sequence of nodes.
   */
  def extractJsInScriptTags(ns: NodeSeq): Js = {
    // Single traversal, joining all scripts once at the end:
    val scripts = new java.util.StringJoiner(";")
    collectJsInScriptTags(ns, scripts)
    if (scripts.length() == 0) Js.Void else Js(scripts.toString)
  }

  private def collectJsInScriptTags(ns: NodeSeq, scripts: java.util.StringJoiner): Unit = ns match {
    case group: Group => group.nodes.foreach(collectJsInScriptTags(_, scripts))
    case node: Node if isJsScriptTag(node) =>
      val sb = new java.lang.StringBuilder()
      node.child.foreach(child => sb.append(child.toString()))
      // Literal (non-regex) replacements:
      val js = sb.toString.replace("\n// <![CDATA[", "").replace("\n// ]]>", "")
      // Empty scripts are skipped (as Js.Void would be by `&`).
      if (js.nonEmpty) scripts.add(js)
    case node: Node => node.child.foreach(collectJsInScriptTags(_, scripts))
    case nodes => nodes.foreach(collectJsInScriptTags(_, scripts))
  }
}

extension (js: Js) {

  def inScriptTag: Elem = JS.inScriptTag(js)

  def inScriptTagModule: Elem = JS.inScriptTagModule(js)

  def printBeforeExec(label: String = "Running code"): Js = {
    println(s"$label: " + js.cmd)
    JS.consoleLog(js.cmd) & js
  }

  def print2Console: Js = {
    JS.consoleLog(js.cmd)
  }
}
