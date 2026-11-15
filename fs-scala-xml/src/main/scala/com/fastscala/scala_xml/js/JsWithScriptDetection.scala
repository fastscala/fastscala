package com.fastscala.scala_xml.js

import com.fastscala.js.Js

import scala.xml.NodeSeq

/**
 * JavaScript with `ns`'s XML in it, as a string literal, between `before` and `after`; the JavaScript of the XML's
 * `<script>` tags (see [[JsXmlUtils.extractJsFromScriptTags]]) is taken out of it, and run after: `scripts` of it (e.g.
 * as it is), as a statement of its own.
 *
 * The scripts are looked for when it's first written (or its cmd built), once: see [[js]].
 */
class JsWithScriptDetection(
                             val before: Js,
                             val ns: NodeSeq,
                             val after: Js,
                             val scripts: Js => Js,
                           ) extends Js {

  /**
   * What this is, as Js that's written part by part (XmlJsStream writes this: the XML as it's serialized, escaped):
   * `before`, the XML without its scripts (as a string literal), `after`; and then the scripts.
   */
  lazy val js: Js = {
    val (cleaned, extracted) = JS.extractJsFromScriptTags(ns)
    Js(before, JS.htmlAsJsStr(cleaned), after) & scripts(extracted)
  }

  override def cmd: String = js.cmd
}
