package com.fastscala.scala_xml.js

import com.fastscala.core.FSContext
import com.fastscala.js.{Js, JsUtils, RawJs}
import com.fastscala.scala_xml.rerenderers.{ContentRerenderer, ContentRerendererP, Rerenderer, RerendererP}

import com.fastscala.utils.IdGen
import org.apache.commons.text.StringEscapeUtils

import scala.xml.*

object JS extends JsXmlUtils

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
    JS(s"document.body.appendChild(${htmlToElement(ns, elemId).cmd})") &
      removeId(elemId)
  }

  def append2(id: String, ns: NodeSeq): Js = {
    val elemId = IdGen.id("template")
    JS(s"""document.getElementById("${escapeStr(id)}").appendChild(${htmlToElement(ns, elemId).cmd})""") &
      removeId(elemId)
  }

  def prepend2(id: String, ns: NodeSeq): Js = {
    val elemId = IdGen.id("template")
    JS(s"""document.getElementById("${escapeStr(id)}").insertBefore(${htmlToElement(ns, elemId).cmd}, document.getElementById("${escapeStr(id)}").firstChild)""") &
      removeId(elemId)
  }

  def replaceWithScriptExtraction(id: String, by: NodeSeq): Js =
    replace(id, excludeJsInScriptTags(by)) & extractJsInScriptTags(by)

  def replace(id: String, by: NodeSeq): Js = JS(
    s"""(document.getElementById("${escapeStr(id)}") ? document.getElementById("${escapeStr(id)}").replaceWith(${
      htmlToElement(
        by
      ).cmd
    }) : console.error("Element with id ${escapeStr(id)} not found"));""")

  def setContents(id: String, ns: NodeSeq): Js = JS(s"""document.getElementById("${escapeStr(id)}").innerHTML = "${StringEscapeUtils.escapeEcmaScript(ns.toString())}"; """)

  def setContents(id: String, js: Js): Js = JS(s"""document.getElementById("${escapeStr(id)}").innerHTML = $js; """)

  def htmlToElement(html: NodeSeq, templateId: String = IdGen.id): Js = JS {
    s"""(document.body.appendChild((function htmlToElement(html) {var template = document.createElement('template');template.setAttribute("id", "$templateId");template.innerHTML = html.trim(); return template;})("${
      StringEscapeUtils.escapeEcmaScript(
        html.toString()
      )
    }"))).content"""
  }

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

  def excludeJsInScriptTags(ns: NodeSeq): NodeSeq = ns match {
    case node: Elem if node.label == "script" &&
      node.attribute("type").flatMap(_.headOption).map(_.toString)
        .exists(str => str == "text/javascript" || str == "module") =>
      NodeSeq.Empty
    case node: Elem => new Elem(prefix = node.prefix, label = node.label, attributes1 = node.attributes, scope = node.scope, minimizeEmpty = node.minimizeEmpty, child = excludeJsInScriptTags(node.child: NodeSeq)*)
    case other: Seq[Node] => other
  }

  def extractJsInScriptTags(ns: NodeSeq): Js = ns match {
    case node: Node if node.label == "script" &&
      node.attribute("type").flatMap(_.headOption).map(_.toString)
        .exists(str => str == "text/javascript" || str == "module") =>
      Js(node.child.toString().replaceAll("\\n// <!\\[CDATA\\[", "").replaceAll("\\n// \\]\\]>", ""))
    case node: Node => node.child.map(extractJsInScriptTags).reduceOption(_ & _).getOrElse(Js.Void)
    case _ => Js.Void
  }
}

extension (js: Js) {

  def inScriptTag: Elem = JS.inScriptTag(js)

  def inScriptTagModule: Elem = JS.inScriptTagModule(js)

  def printBeforeExec: Js = {
    println("> " + js.cmd)
    JS.consoleLog(js.cmd) & js
  }

  def print2Console: Js = {
    JS.consoleLog(js.cmd)
  }
}
