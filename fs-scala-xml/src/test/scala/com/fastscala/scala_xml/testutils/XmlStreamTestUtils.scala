package com.fastscala.scala_xml.testutils

import com.fastscala.scala_xml.xml.XmlJsStream
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import scala.xml.{Node, NodeSeq}

/** Writing with [[XmlJsStream]] (which writes UTF-8 into a Content.Sink) in tests. */
object XmlStreamTestUtils {

  /** Records what's written: the bytes, and each write's size and `last`. Each write completes at once. */
  final class RecordingSink extends Content.Sink {
    val bytes = new ByteArrayOutputStream()
    val writes = collection.mutable.ArrayBuffer[(Int, Boolean)]()

    override def write(last: Boolean, byteBuffer: ByteBuffer, callback: Callback): Unit = {
      if (writes.lastOption.exists(_._2)) callback.failed(new IllegalStateException("a write after the last one"))
      else {
        val n = byteBuffer.remaining()
        val copy = new Array[Byte](n)
        byteBuffer.get(copy)
        bytes.write(copy)
        writes += ((n, last))
        callback.succeeded()
      }
    }

    def asString: String = bytes.toString(UTF_8)
  }

  /** `ns` written by XmlJsStream, decoded from UTF-8. */
  def writtenSeq(ns: NodeSeq, prefix: String = ""): String = { val s = new RecordingSink; XmlJsStream.writeSeq(ns, s, prefix); s.asString }

  /** `n` written by XmlJsStream, decoded from UTF-8. */
  def writtenNode(n: Node): String = { val s = new RecordingSink; XmlJsStream.writeNode(n, s); s.asString }

  /** `s` through UTF-8 and back: as `s` comes out of XmlJsStream (a lone surrogate becomes '?', as String.getBytes does). */
  def utf8RoundTrip(s: String): String = new String(s.getBytes(UTF_8), UTF_8)
}
