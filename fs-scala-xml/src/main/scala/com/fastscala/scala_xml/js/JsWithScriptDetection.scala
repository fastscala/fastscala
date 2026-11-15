package com.fastscala.scala_xml.js

import com.fastscala.js.Js

import scala.xml.NodeSeq

/**
 * JavaScript with `ns`'s XML in it, as a string literal, between `before` and `after`; the JavaScript of the XML's
 * `<script>` tags (see [[JsXmlUtils.extractJsFromScriptTags]]) is taken out of it, and run after: `scripts` of it (e.g.
 * as it is), as a statement of its own.
 *
 * XmlJsStream writes it without looking for the scripts first: it writes the XML, and collects the scripts' JavaScript
 * (instead of writing them) as it gets to them; `scripts` is called with it once the XML is written. Its [[cmd]] is the
 * same code: there, the scripts are looked for when it's first built, once (see [[js]]).
 */
class JsWithScriptDetection(
                             val before: Js,
                             val ns: NodeSeq,
                             val after: Js,
                             val scripts: Js => Js,
                           ) extends Js {

  /**
   * What this is, as Js (what its [[cmd]] is built from): `before`, the XML without its scripts (as a string literal),
   * `after`; and then the scripts.
   */
  lazy val js: Js = {
    val (cleaned, extracted) = JS.extractJsFromScriptTags(ns)
    Js(before, JS.htmlAsJsStr(cleaned), after) & scripts(extracted)
  }

  override def cmd: String = js.cmd
}
