package com.fastscala.js

import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import org.scalatest.flatspec.AnyFlatSpec

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class CompositeJsSpec extends AnyFlatSpec {

  private def streamed(js: Js): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    js.writeUtf8Enc(new Content.Sink {
      override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = {
        val arr = new Array[Byte](bb.remaining()); bb.get(arr); out.write(arr); cb.succeeded()
      }
    }, Callback.NOOP)
    out.toByteArray
  }

  private def check(js: Js, expectedCmd: String): Unit = {
    assert(js.cmd == expectedCmd)
    val bytes = streamed(js)
    assert(new String(bytes, StandardCharsets.UTF_8) == expectedCmd)
    assert(js.lengthUtf8Enc == bytes.length)
  }

  /** A sink that completes writes only when `completeNext` is called, and fails if a write starts while one is pending. */
  private class AsyncSink extends Content.Sink {
    val out = new ByteArrayOutputStream()
    var pending: Option[Callback] = None
    var overlapped = false
    var failNextWith: Option[Throwable] = None

    override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = {
      if (pending.isDefined) overlapped = true
      val arr = new Array[Byte](bb.remaining()); bb.get(arr); out.write(arr)
      pending = Some(cb)
    }

    def completeNext(): Unit = {
      val cb = pending.get
      pending = None
      failNextWith match {
        case Some(t) => failNextWith = None; cb.failed(t)
        case None => cb.succeeded()
      }
    }

    def drain(): Unit = while (pending.isDefined) completeNext()
  }

  private class RecordingCallback extends Callback {
    var successes = 0
    var failures = List.empty[Throwable]

    override def succeeded(): Unit = successes += 1

    override def failed(x: Throwable): Unit = failures ::= x
  }

  "CompositeJs" should "concatenate children without a separator" in {
    check(new CompositeJs(Js("a()"), Js("b()")), "a()b()")
  }

  "withSeparator" should "put the separator only between children" in {
    check(new CompositeJs(Js("a()"), Js("b()"), Js("c()")).withSeparator(Js(";")), "a();b();c()")
  }

  it should "add no separator for a single child" in {
    check(new CompositeJs(Js("a()")).withSeparator(Js(";")), "a()")
  }

  it should "be empty with no children" in {
    check(new CompositeJs().withSeparator(Js(";")), "")
  }

  it should "count multi-byte separators correctly" in {
    check(new CompositeJs(Js("é"), Js("€")).withSeparator(Js("/*😀*/")), "é/*😀*/€")
  }

  "writeUtf8Enc" should "not start a write before the previous one completed, and complete the callback once at the end" in {
    val js = new CompositeJs(Js("a()"), Js("b()"), Js("c()")).withSeparator(Js(";"))
    val sink = new AsyncSink
    val cb = new RecordingCallback
    js.writeUtf8Enc(sink, cb)

    assert(new String(sink.out.toByteArray, StandardCharsets.UTF_8) == "a()") // only the first part so far
    assert(cb.successes == 0)
    sink.drain()
    assert(!sink.overlapped)
    assert(new String(sink.out.toByteArray, StandardCharsets.UTF_8) == "a();b();c()")
    assert(cb.successes == 1)
    assert(cb.failures.isEmpty)
  }

  it should "complete the callback once, synchronously, for a synchronous sink" in {
    val cb = new RecordingCallback
    new CompositeJs(Js("a()"), Js("b()")).withSeparator(Js(";")).writeUtf8Enc(new Content.Sink {
      override def write(last: Boolean, bb: ByteBuffer, c: Callback): Unit = c.succeeded()
    }, cb)
    assert(cb.successes == 1)
  }

  it should "complete the callback once when there are no children" in {
    val sink = new AsyncSink
    val cb = new RecordingCallback
    new CompositeJs().writeUtf8Enc(sink, cb)
    assert(sink.pending.isEmpty)
    assert(cb.successes == 1)
  }

  it should "fail the callback once and stop writing when a write fails" in {
    val js = new CompositeJs(Js("a()"), Js("b()"), Js("c()"))
    val sink = new AsyncSink
    val cb = new RecordingCallback
    val boom = new RuntimeException("boom")
    js.writeUtf8Enc(sink, cb)
    sink.completeNext() // a() ok
    sink.failNextWith = Some(boom)
    sink.completeNext() // b() fails
    assert(sink.pending.isEmpty) // c() never written
    assert(new String(sink.out.toByteArray, StandardCharsets.UTF_8) == "a()b()")
    assert(cb.successes == 0)
    assert(cb.failures == List(boom))
  }

  it should "sequence the parts of nested CompositeJs" in {
    val js = Js(Js("a("), new CompositeJs(Js("b"), Js("c")).withSeparator(Js(", ")), Js(")"))
    val sink = new AsyncSink
    val cb = new RecordingCallback
    js.writeUtf8Enc(sink, cb)
    sink.drain()
    assert(!sink.overlapped)
    assert(new String(sink.out.toByteArray, StandardCharsets.UTF_8) == "a(b, c)")
    assert(new String(sink.out.toByteArray, StandardCharsets.UTF_8) == js.cmd)
    assert(cb.successes == 1)
  }
}
