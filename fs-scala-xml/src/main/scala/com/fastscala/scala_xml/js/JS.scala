package com.fastscala.scala_xml.js

import com.fastscala.core.FSContext
import com.fastscala.js.{CompositeJs, Js, JsUtils}
import com.fastscala.scala_xml.rerenderers.{ContentRerenderer, ContentRerendererP, Rerenderer, RerendererP}
import com.fastscala.scala_xml.xml.ElemWithJsInScript
import com.fastscala.utils.IdGen
import org.apache.commons.text.StringEscapeUtils
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import org.owasp.encoder.{Encode, EncodedWriter, Encoders}

import java.io.{OutputStream, OutputStreamWriter}
import java.nio.ByteBuffer
import scala.xml.*

object JS extends JsXmlUtils

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
