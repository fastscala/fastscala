package com.fastscala.utils

import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback

import java.io.Writer
import java.nio.charset.{CodingErrorAction, StandardCharsets}
import java.nio.{ByteBuffer, CharBuffer}

/**
 * A [[Content.Sink]] that decodes the UTF-8 bytes written to it and writes the chars to `writer`: so what writes to
 * a Sink (e.g. [[XmlJsStream]]) can write through a Writer (e.g. an encoding one). The last write only flushes the
 * writer: it's never closed, so whatever is under it isn't ended.
 *
 * Each write is done (decoded and written) before its callback is completed, and before it returns.
 */
final class Utf8ToWriterSink(writer: Writer) extends Content.Sink {

  private val decoder = StandardCharsets.UTF_8.newDecoder()
    .onMalformedInput(CodingErrorAction.REPLACE)
    .onUnmappableCharacter(CodingErrorAction.REPLACE)

  private val chars = CharBuffer.allocate(8192)

  /** The start of a UTF-8 sequence split between two writes (at most 3 bytes; XmlJsStream never splits one). */
  private val carry = ByteBuffer.allocate(8)

  override def write(last: Boolean, bytes: ByteBuffer, callback: Callback): Unit =
    try {
      // Complete the split sequence first: one byte at a time, until the decoder has taken it (so nothing after it is
      // decoded before it).
      while (carry.position() > 0 && bytes.hasRemaining) {
        carry.put(bytes.get())
        carry.flip()
        decode(carry, endOfInput = false)
        carry.compact()
      }
      if (carry.position() == 0) {
        decode(bytes, endOfInput = last)
        if (bytes.hasRemaining) carry.put(bytes) // an incomplete sequence at the end: decoded with the next write
      }
      if (last) finish()
      callback.succeeded()
    } catch {
      case t: Throwable => callback.failed(t)
    }

  /**
   * Ends the input: decodes what's left (an incomplete sequence, as malformed), and flushes the writer (never closing
   * it). Done by the last write; call it when the content is written without one (e.g. a Js part of a response, whose
   * writes never end the content). Then the sink can be written to again, as new content.
   */
  def finish(): Unit = {
    // The end of the input (the decoder must be told before it's flushed): with what's left, if anything (the content
    // ended in the middle of a sequence: decoded as malformed).
    carry.flip()
    decode(carry, endOfInput = true)
    decoder.flush(chars)
    drain()
    decoder.reset()
    carry.clear()
    writer.flush()
  }

  private def decode(in: ByteBuffer, endOfInput: Boolean): Unit = {
    var overflow = true
    while (overflow) {
      overflow = decoder.decode(in, chars, endOfInput).isOverflow
      drain()
    }
  }

  private def drain(): Unit = {
    chars.flip()
    if (chars.hasRemaining) writer.write(chars.array(), chars.arrayOffset() + chars.position(), chars.remaining())
    chars.clear()
  }
}
