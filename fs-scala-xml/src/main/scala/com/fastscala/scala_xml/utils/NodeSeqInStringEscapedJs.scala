package com.fastscala.scala_xml.utils

import com.fastscala.js.Js
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import org.owasp.encoder.{EncodedWriter, Encoders}

import java.io.{OutputStream, OutputStreamWriter}
import scala.xml.NodeSeq


/**
 * `ns`'s XML, escaped for a JavaScript string. Streamed: [[XmlStream]] writes the XML (as UTF-8 bytes) into a
 * [[Utf8ToWriterSink]], which decodes it into an [[EncodedWriter]] (JavaScript escaping) on an OutputStreamWriter
 * (UTF-8) on the output. The XML is never a single String; nor is the content ended (it's inside a JS response).
 */
class NodeSeqInStringEscapedJs(ns: NodeSeq) extends Js {
  private def encodedWriter(out: OutputStream): EncodedWriter =
    new EncodedWriter(new OutputStreamWriter(out, "UTF-8"), Encoders.JAVASCRIPT)

  /** Writes the escaped XML to `out` (flushed, not closed). */
  private def writeEscaped(out: OutputStream): Unit =
    XmlStream.writeSeq(ns, new Utf8ToWriterSink(encodedWriter(out))) // its last write flushes the writers
  
  override def writeUtf8Enc(sink: Content.Sink, callback: Callback): Unit = {
    // Not closed: closing the sink's OutputStream would write last=true and end the response.
    try {
      writeEscaped(Content.Sink.asOutputStream(sink)) // flushed before signalling completion
    } catch {
      case t: Throwable =>
        callback.failed(t)
        return
    }
    callback.succeeded()
  }

  override def cmd: String = org.owasp.encoder.Encode.forJavaScript(ns.toString())
}