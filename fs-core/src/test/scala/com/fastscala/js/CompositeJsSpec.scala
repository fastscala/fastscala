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

  "CompositeJs.joined" should "put the separator only between children" in {
    check(CompositeJs.joined(Seq(Js("a()"), Js("b()"), Js("c()")), Js(";")), "a();b();c()")
  }

  it should "add no separator for a single child" in {
    check(CompositeJs.joined(Seq(Js("a()")), Js(";")), "a()")
  }

  it should "be empty with no children" in {
    check(CompositeJs.joined(Seq(), Js(";")), "")
  }

  it should "count multi-byte separators correctly" in {
    check(CompositeJs.joined(Seq(Js("é"), Js("€")), Js("/*😀*/")), "é/*😀*/€")
  }

  "writeUtf8Enc" should "not start a write before the previous one completed, and complete the callback once at the end" in {
    val js = CompositeJs.joined(Seq(Js("a()"), Js("b()"), Js("c()")), Js(";"))
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
    CompositeJs.joined(Seq(Js("a()"), Js("b()")), Js(";")).writeUtf8Enc(new Content.Sink {
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
    val js = Js(Js("a("), CompositeJs.joined(Seq(Js("b"), Js("c")), Js(", ")), Js(")"))
    val sink = new AsyncSink
    val cb = new RecordingCallback
    js.writeUtf8Enc(sink, cb)
    sink.drain()
    assert(!sink.overlapped)
    assert(new String(sink.out.toByteArray, StandardCharsets.UTF_8) == "a(b, c)")
    assert(new String(sink.out.toByteArray, StandardCharsets.UTF_8) == js.cmd)
    assert(cb.successes == 1)
  }

  // ---------- foreachPart ----------

  private def parts(js: Js): Seq[String] = {
    val b = Seq.newBuilder[String]
    js.foreachPart(b += _)
    b.result()
  }

  "foreachPart" should "give parts that, concatenated, are exactly cmd" in {
    val cases: Seq[Js] = Seq(
      Js("a()"),
      Js.Void,
      Js(""),
      new CompositeJs(Js("a()"), Js("b()")),
      CompositeJs.joined(Seq(Js("a()"), Js("b()"), Js("c()")), Js(";")),
      CompositeJs.joined(Seq(Js("a()")), Js(";")),
      CompositeJs.joined(Seq(), Js(";")),
      CompositeJs.joined(Seq(Js("x"), Js.Void, Js("y")), Js(", ")),
      CompositeJs.joined(Seq(CompositeJs.joined(Seq(Js("a"), Js("b")), Js("|")), Js("c")), Js(";")),
      Js("a()") & Js("b()") & Js("c()") & Js.Void & Js("d()"),
      new JsFunc0(Js("x()")),
      new JsFunc1(arg => Js(s"f(${arg.cmd})")),
      new JsFunc3((a, b, c) => Js(a, Js(","), b, Js(","), c)),
      CompositeJs.joined(Seq(Js("é"), Js("€")), Js("/*😀*/")),
    )
    cases.foreach(js => assert(parts(js).mkString == js.cmd, js.cmd))
  }

  it should "give a StringJs's string, and a CompositeJs's parts, without building cmd" in {
    assert(parts(Js("abc")) == Seq("abc"))
    assert(parts(CompositeJs.joined(Seq(Js("a"), Js("b"), Js("c")), Js(";"))) == Seq("a", ";", "b", ";", "c"))
    // A Js whose cmd isn't called (it would throw): only its parts are.
    val noCmd = new CompositeJs(Js("a"), Js("b")) {
      override def cmd: String = throw new IllegalStateException("cmd was built")
    }
    assert(parts(noCmd) == Seq("a", "b"))
  }

  it should "give a Js's cmd, as one part, by default" in {
    val js = new Js {
      override def writeUtf8Enc(sink: Content.Sink, callback: Callback): Unit = callback.succeeded()
      override def cmd: String = "f()"
    }
    assert(parts(js) == Seq("f()"))
  }

  // ---------- JsFunc ----------

  "JsFunc0..3" should "be functions of the body, as JavaScript" in {
    assert(new JsFunc0(Js("x()")).cmd == "function(){x()}")
    assert(new JsFunc1(a => Js(s"f(${a.cmd})")).cmd == "function(arg1){f(arg1)}")
    assert(new JsFunc2((a, b) => Js(s"f(${a.cmd}, ${b.cmd})")).cmd == "function(arg1, arg2){f(arg1, arg2)}")
    assert(new JsFunc3((a, b, c) => Js(s"f(${a.cmd}, ${b.cmd}, ${c.cmd})")).cmd == "function(arg1, arg2, arg3){f(arg1, arg2, arg3)}")
    assert(JsFunc1(a => Js(s"g(${a.cmd})")).cmd == "function(arg1){g(arg1)}") // (without `new`)
  }

  it should "call the body once (it can have side effects, e.g. registering a callback)" in {
    var calls = 0
    new JsFunc1(a => { calls += 1; a })
    assert(calls == 1)
    calls = 0
    new JsFunc2((a, _) => { calls += 1; a })
    assert(calls == 1)
    calls = 0
    new JsFunc3((a, _, _) => { calls += 1; a })
    assert(calls == 1)
  }

  it should "still be subtypes of each other" in {
    val f0: JsFunc1 = new JsFunc0(Js("x()"))
    val f1: JsFunc2 = new JsFunc1(a => a)
    val f2: JsFunc3 = new JsFunc2((a, _) => a)
    val f3: CompositeJs = new JsFunc3((a, _, _) => a)
    assert(Seq(f0, f1, f2, f3).forall(_.cmd.startsWith("function(")))
  }
}
