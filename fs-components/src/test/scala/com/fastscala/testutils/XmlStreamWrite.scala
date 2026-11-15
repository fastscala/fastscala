package com.fastscala.testutils

import com.fastscala.scala_xml.xml.XmlJsStream
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import scala.xml.Node

/** Writing with [[XmlJsStream]] (which writes UTF-8 into a Content.Sink) in tests. */
object XmlStreamWrite {

  /** Discards what's written, counting the bytes; each write completes at once (so only writing is measured). */
  final class CountingSink extends Content.Sink {
    var count = 0L

    override def write(last: Boolean, byteBuffer: ByteBuffer, callback: Callback): Unit = {
      count += byteBuffer.remaining()
      byteBuffer.position(byteBuffer.limit())
      callback.succeeded()
    }
  }

  /** `n` written by XmlJsStream, decoded from UTF-8. */
  def writtenNode(n: Node): String = {
    val bytes = new ByteArrayOutputStream()
    XmlJsStream.writeNode(n, new Content.Sink {
      override def write(last: Boolean, byteBuffer: ByteBuffer, callback: Callback): Unit = {
        while (byteBuffer.hasRemaining) bytes.write(byteBuffer.get())
        callback.succeeded()
      }
    })
    bytes.toString(UTF_8)
  }

  /** Writes `n` with XmlJsStream, discarding the output: returns how many bytes were written. */
  def countWritten(n: Node): Long = {
    val sink = new CountingSink
    XmlJsStream.writeNode(n, sink)
    sink.count
  }
}
