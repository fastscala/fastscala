package com.fastscala.scala_xml.js

import com.fastscala.js.Js

import scala.xml.NodeSeq

/**
 * `ns`'s XML, as JavaScript code (e.g. to put in a string literal, escaped: `new EncJs(new NodeSeqJs(ns), inString =
 * true)`). XmlJsStream writes it as XML, node by node: the XML is never one String (only [[cmd]] builds it).
 */
class NodeSeqJs(val ns: NodeSeq) extends Js {
  override def cmd: String = ns.toString
}
