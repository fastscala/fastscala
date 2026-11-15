package com.fastscala.js

import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import org.scalatest.flatspec.AnyFlatSpec

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

/** [[JsWriter.Cmd]]: the Js's cmd, in UTF-8, in one write. */
class JsWriterSpec extends AnyFlatSpec {

  /** Records the writes (their text, and `last`); completes each at once, or fails it with `failWith`. */
  private class RecordingSink(failWith: Throwable = null) extends Content.Sink {
    var writes = Vector.empty[(String, Boolean)]

    override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = {
      val arr = new Array[Byte](bb.remaining())
      bb.get(arr)
      writes :+= (new String(arr, UTF_8), last)
      if (failWith eq null) cb.succeeded() else cb.failed(failWith)
    }
  }

  private class RecordingCallback extends Callback {
    var successes = 0
    var failures = List.empty[Throwable]

    override def succeeded(): Unit = successes += 1

    override def failed(x: Throwable): Unit = failures ::= x
  }

  private val js = Js(Js("f("), new EncJs(Js("é \"q\" 😀"), inString = true), Js(");"))

  "JsWriter.Cmd" should "write the cmd, in UTF-8, in one write that ends the content only if endContent" in {
    for (endContent <- Seq(true, false)) {
      val (sink, cb) = (new RecordingSink, new RecordingCallback)
      JsWriter.Cmd.writeJs(js, sink, cb, endContent)
      assert(sink.writes == Vector((js.cmd, endContent)))
      assert(cb.successes == 1 && cb.failures.isEmpty)
    }
  }

  it should "write an empty Js as one empty write" in {
    val (sink, cb) = (new RecordingSink, new RecordingCallback)
    JsWriter.Cmd.writeJs(Js.Void, sink, cb, endContent = true)
    assert(sink.writes == Vector(("", true)))
    assert(cb.successes == 1)
  }

  it should "write a null cmd as `null`" in {
    val (sink, cb) = (new RecordingSink, new RecordingCallback)
    JsWriter.Cmd.writeJs(Js(null: String), sink, cb, endContent = false)
    assert(sink.writes == Vector(("null", false)))
    assert(cb.successes == 1)
  }

  it should "fail the callback, once, if the cmd can't be built (nothing is written)" in {
    val boom = new IllegalStateException("boom")
    val failing = new Js {
      override def cmd: String = throw boom
    }
    val (sink, cb) = (new RecordingSink, new RecordingCallback)
    JsWriter.Cmd.writeJs(Js(Js("a"), failing), sink, cb, endContent = true)
    assert(sink.writes.isEmpty)
    assert(cb.successes == 0 && cb.failures == List(boom))
  }

  it should "fail the callback, once, if the write fails" in {
    val boom = new RuntimeException("boom")
    val (sink, cb) = (new RecordingSink(boom), new RecordingCallback)
    JsWriter.Cmd.writeJs(js, sink, cb, endContent = true)
    assert(cb.successes == 0 && cb.failures == List(boom))
  }
}
