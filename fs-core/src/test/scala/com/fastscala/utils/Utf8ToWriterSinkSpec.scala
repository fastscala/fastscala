package com.fastscala.utils

import org.eclipse.jetty.util.Callback
import org.scalatest.flatspec.AnyFlatSpec

import java.io.{StringWriter, Writer}
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

/** [[Utf8ToWriterSink]]: the UTF-8 bytes written to it, decoded, written to a Writer, which is flushed but not closed. */
class Utf8ToWriterSinkSpec extends AnyFlatSpec {

  /** Records how the callback was completed. */
  private final class RecordingCallback extends Callback {
    var successes = 0
    var failure: Option[Throwable] = None

    override def succeeded(): Unit = successes += 1

    override def failed(x: Throwable): Unit = failure = Some(x)
  }

  private final class FlagWriter extends StringWriter {
    var flushes = 0
    var closed = false

    override def flush(): Unit = { flushes += 1; super.flush() }

    override def close(): Unit = closed = true
  }

  /** Writes `bytes` to a new sink in pieces of `pieceSize` bytes (the last one with last = true). */
  private def writeInPieces(bytes: Array[Byte], pieceSize: Int): (FlagWriter, Seq[RecordingCallback]) = {
    val writer = new FlagWriter
    val sink = new Utf8ToWriterSink(writer)
    val pieces = if (bytes.isEmpty) Seq(Array.emptyByteArray) else bytes.grouped(pieceSize).toSeq
    val callbacks = pieces.zipWithIndex.map { case (piece, i) =>
      val cb = new RecordingCallback
      sink.write(i == pieces.size - 1, ByteBuffer.wrap(piece), cb)
      cb
    }
    (writer, callbacks)
  }

  private val text = "plain, é ü ç, € 日本, 😀👍🏽 𝄞, \u007f\u0080߿ࠀ￿, and " + ("long text é€😀 " * 2000)

  "Utf8ToWriterSink" should "decode the bytes into the writer, whatever the size of the writes (even splitting chars)" in {
    for (pieceSize <- Seq(1, 2, 3, 5, 7, 4096, 100_000)) {
      val (writer, callbacks) = writeInPieces(text.getBytes(UTF_8), pieceSize)
      assert(writer.toString == text, s"piece size $pieceSize")
      assert(callbacks.forall(cb => cb.successes == 1 && cb.failure.isEmpty), s"piece size $pieceSize")
    }
  }

  it should "flush the writer on the last write, and never close it" in {
    val (writer, _) = writeInPieces("abc é".getBytes(UTF_8), 2)
    assert(writer.flushes >= 1 && !writer.closed)
  }

  it should "handle an empty content" in {
    val (writer, callbacks) = writeInPieces(Array.emptyByteArray, 1)
    assert(writer.toString == "" && callbacks.map(_.successes) == Seq(1))
  }

  it should "decode a sequence cut off by the end of the content as the replacement char" in {
    val bytes = "ab".getBytes(UTF_8) ++ "€".getBytes(UTF_8).take(2) // '€' is 3 bytes: only 2 of them
    for (pieceSize <- Seq(1, 2, 100)) {
      val (writer, _) = writeInPieces(bytes, pieceSize)
      assert(writer.toString == "ab�", s"piece size $pieceSize")
    }
  }

  it should "decode malformed bytes as the replacement char, and go on" in {
    val bytes = "a".getBytes(UTF_8) ++ Array(0xff.toByte) ++ "b€".getBytes(UTF_8)
    val (writer, _) = writeInPieces(bytes, 1)
    assert(writer.toString == "a�b€")
  }

  it should "fail the callback if the writer fails" in {
    val failing = new Writer {
      override def write(cbuf: Array[Char], off: Int, len: Int): Unit = throw new java.io.IOException("gone")
      override def flush(): Unit = ()
      override def close(): Unit = ()
    }
    val cb = new RecordingCallback
    new Utf8ToWriterSink(failing).write(true, ByteBuffer.wrap("x".getBytes(UTF_8)), cb)
    assert(cb.successes == 0 && cb.failure.exists(_.getMessage == "gone"))
  }
}
