package com.fastscala.xml.scala_xml

import com.fastscala.js.{Js, JsXmlUtils, RichJsXmlUtils}
import com.fastscala.js.rerenderers.{ContentRerenderer, ContentRerendererP, Rerenderer, RerendererP}

object JS extends JsXmlUtils[FSScalaXmlEnv.type]()(FSScalaXmlSupport.fsXmlSupport) {

  type ScalaXmlRerenderer = Rerenderer[FSScalaXmlEnv.type]
  type ScalaXmlContentRerenderer = ContentRerenderer[FSScalaXmlEnv.type]
  type ScalaXmlContentRerendererP[T] = ContentRerendererP[FSScalaXmlEnv.type, T]
  type ScalaXmlRerendererP[T] = RerendererP[FSScalaXmlEnv.type, T]

  implicit class RichJs(val js: Js) extends AnyVal {
    def inScriptTag: scala.xml.Elem = JS.inScriptTag(js)

    def printBeforeExec: Js = {
      println("> " + js.cmd)
      JS.consoleLog(js.cmd) & js
    }
  }
}
