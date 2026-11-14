package com.fastscala.scala_xml.js

import com.fastscala.js.Js
import com.fastscala.scala_xml.xml.XmlStream
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback

import scala.xml.NodeSeq

/**
 * `ns`'s XML, as JavaScript code (e.g. to put in a string literal, escaped: `new EncJs(new NodeSeqJs(ns), inString =
 * true)`). Written by [[XmlStream]], without blocking, as a part of the content: the XML is never one String (only
 * [[cmd]] builds it).
 */
class NodeSeqJs(ns: NodeSeq) extends Js {
  override def cmd: String = ns.toString

  override def writeUtf8Enc(sink: Content.Sink, callback: Callback): Unit =
    XmlStream.writeSeq(ns, sink, "", callback, endContent = false) // (a part of the content: not ending it)
}
