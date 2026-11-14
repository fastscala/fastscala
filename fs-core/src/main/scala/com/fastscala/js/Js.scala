package com.fastscala.js

import Js as JS

import com.fastscala.core.FSContext
import com.fastscala.utils.IdGen
import org.apache.commons.text.StringEscapeUtils
import org.eclipse.jetty.http.{HttpHeader, MimeTypes}
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.Response
import org.eclipse.jetty.util.{BufferUtil, Callback, IteratingCallback}
import org.owasp.encoder.Encode

import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.Date

trait Js {

  def lengthUtf8Enc: Long

  def writeUtf8Enc(sink: Content.Sink, callback: Callback): Unit

  def cmd: String

  /**
   * Calls `f` with the parts of [[cmd]], in order: `cmd` is them concatenated. So `cmd` can be written (e.g. escaped,
   * by XmlStream, into an attribute) without being built. By default, `cmd` itself, as one part.
   */
  def foreachPart(f: String => Unit): Unit = f(cmd)

  def &(js: Js) = (this, js) match {
    case (Js.Void, Js.Void) => Js.Void
    case (Js.Void, js) => js
    case (js, Js.Void) => js
    case (js1, js2) => Js(js1, Js(";"), js2)
  }

  def isVoid = cmd.trim == ""

  def onDOMContentLoaded: Js = JS(
    Js("(function () {var load = function() {"),
    this,
    Js(";};if (/complete|interactive|loaded/.test(document.readyState)) {load();}else { document.addEventListener('DOMContentLoaded', function() {load();}, false); }})();"),
  )

  override def toString: String = cmd
}

object Js {

  val Void = com.fastscala.js.Void

  def apply(js: String): Js = if (js == "") Void else StringJs(js)

  def apply(js: Js*): Js = if (js.isEmpty) Void else CompositeJs(js *)
}
