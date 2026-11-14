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


/*
 * JavaScript functions, `function(...){<body>}`. Each class builds its children once (calling its body once), and
 * passes them up through the protected constructor: overriding `children` instead (as before) made every superclass
 * build its own too, so a body ran several times (2 for a JsFunc2, 3 for a JsFunc1): with side effects (e.g. registering
 * a callback, as AceEditor's change listener does), they happened that many times.
 */

class JsFunc0 protected(parts: Seq[Js]) extends JsFunc1(parts) {
  def this(body0: Js) = this(Seq(Js("function(){"), body0, Js("}")))
}

class JsFunc1 protected(parts: Seq[Js]) extends JsFunc2(parts) {
  def this(body1: Js => Js) = this(Seq(Js("function(arg1){"), body1(Js("arg1")), Js("}")))
}

class JsFunc2 protected(parts: Seq[Js]) extends JsFunc3(parts) {
  def this(body2: (Js, Js) => Js) = this(Seq(Js("function(arg1, arg2){"), body2(Js("arg1"), Js("arg2")), Js("}")))
}

class JsFunc3 protected(parts: Seq[Js]) extends CompositeJs(parts *) {
  def this(body3: (Js, Js, Js) => Js) = this(Seq(Js("function(arg1, arg2, arg3){"), body3(Js("arg1"), Js("arg2"), Js("arg3")), Js("}")))
}
