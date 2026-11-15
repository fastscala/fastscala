package com.fastscala.js

/**
 * `js`, escaped for a JavaScript string literal (with [[JsEscape]]); with `inString`, quoted too: a
 * string literal whose value is `js`'s code (e.g. `new EncJs(new NodeSeqJs(ns), inString = true)`, HTML for innerHTML).
 *
 * [[cmd]] builds the escaped code as one String; fs-scala-xml's `XmlJsStream` writes it escaped as it goes, without
 * building it.
 */
class EncJs(val js: Js, val inString: Boolean = false) extends Js {

  override def cmd: String = {
    if (inString) '"' + JsEscape.escape(js.cmd) + '"'
    else JsEscape.escape(js.cmd)
  }
}
