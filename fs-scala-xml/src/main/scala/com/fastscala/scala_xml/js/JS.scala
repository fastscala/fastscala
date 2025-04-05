package com.fastscala.scala_xml.js

import com.fastscala.core.FSContext
import com.fastscala.js.{Js, JsUtils, RawJs}
import com.fastscala.scala_xml.rerenderers.{ContentRerenderer, ContentRerendererP, Rerenderer, RerendererP}
import com.fastscala.utils.IdGen
import org.apache.commons.text.StringEscapeUtils

import scala.xml.{Elem, NodeSeq}

object JS extends JsXmlUtils

class JsXmlUtils extends JsUtils {

  def rerenderable(render: Rerenderer => FSContext => Elem, idOpt: Option[String] = None, debugLabel: Option[String] = None): Rerenderer =
    new Rerenderer(rerenderer => fsc => (render(rerenderer)(fsc), Js.Void), idOpt = idOpt, debugLabel = debugLabel)

  def rerenderableP[P](render: RerendererP[P] => FSContext => P => Elem, idOpt: Option[String] = None, debugLabel: Option[String] = None): RerendererP[P] =
    new RerendererP[P](rerenderer => fsc => param => (render(rerenderer)(fsc)(param), Js.Void), idOpt = idOpt, debugLabel = debugLabel)

  def rerenderableContents(render: ContentRerenderer => FSContext => NodeSeq, id: Option[String] = None, debugLabel: Option[String] = None): ContentRerenderer =
    new ContentRerenderer(rerenderer => fsc => (render(rerenderer)(fsc), Js.Void), id = id, debugLabel = debugLabel)

  def rerenderableContentsP[P](render: ContentRerendererP[P] => FSContext => P => NodeSeq, id: Option[String] = None, debugLabel: Option[String] = None): ContentRerendererP[P] =
    new ContentRerendererP[P](rerenderer => fsc => param => (render(rerenderer)(fsc)(param), Js.Void), id = id, debugLabel = debugLabel)

  def rerenderableWithJs(render: Rerenderer => FSContext => (Elem, Js), idOpt: Option[String] = None, debugLabel: Option[String] = None): Rerenderer =
    new Rerenderer(render, idOpt = idOpt, debugLabel = debugLabel)

  def rerenderablePWithJs[P](render: RerendererP[P] => FSContext => P => (Elem, Js), idOpt: Option[String] = None, debugLabel: Option[String] = None): RerendererP[P] =
    new RerendererP[P](render, idOpt = idOpt, debugLabel = debugLabel)

  def rerenderableContentsWithJs(render: ContentRerenderer => FSContext => (NodeSeq, Js), id: Option[String] = None, debugLabel: Option[String] = None): ContentRerenderer =
    new ContentRerenderer(render, id = id, debugLabel = debugLabel)

  def rerenderableContentsPWithJs[P](render: ContentRerendererP[P] => FSContext => P => (NodeSeq, Js), id: Option[String] = None, debugLabel: Option[String] = None): ContentRerendererP[P] =
    new ContentRerendererP[P](render, id = id, debugLabel = debugLabel)

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

  def replace(id: String, by: NodeSeq): Js = JS(s"""(document.getElementById("${escapeStr(id)}") ? document.getElementById("${escapeStr(id)}").replaceWith(${htmlToElement(
      by
    ).cmd}) : console.error("Element with id ${escapeStr(id)} not found"));""")

  def setContents(id: String, ns: NodeSeq): Js = JS(s"""document.getElementById("${escapeStr(id)}").innerHTML = "${StringEscapeUtils.escapeEcmaScript(ns.toString())}"; """)
  
  def setContents(id: String, js: Js): Js = JS(s"""document.getElementById("${escapeStr(id)}").innerHTML = $js; """)

  def htmlToElement(html: NodeSeq, templateId: String = IdGen.id): Js = JS {
    s"""(document.body.appendChild((function htmlToElement(html) {var template = document.createElement('template');template.setAttribute("id", "$templateId");template.innerHTML = html.trim(); return template;})("${StringEscapeUtils.escapeEcmaScript(
        html.toString()
      )}"))).content"""
  }

  def forceHttps: Elem = {
    <script type="text/javascript">{
      scala.xml.Unparsed("""//<![CDATA[
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

  def showIf(b: Boolean)(ns: => NodeSeq): NodeSeq = if (b) ns else NodeSeq.Empty
}

extension (js: Js) {

  def inScriptTag: Elem = JS.inScriptTag(js)

  def printBeforeExec: Js = {
    println("> " + js.cmd)
    JS.consoleLog(js.cmd) & js
  }

  def print2Console: Js = {
    JS.consoleLog(js.cmd)
  }
}
