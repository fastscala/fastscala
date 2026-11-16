package com.fastscala.scala_xml.xml

import com.fastscala.scala_xml.xml.streaming.*

import com.fastscala.js.{CompositeJs, EncJs, Js, JsFunc1, JsFunc3, js}
import com.fastscala.scala_xml.js.NodeSeqJs
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import org.scalatest.flatspec.AnyFlatSpec

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.atomic.AtomicInteger
import scala.xml.{NodeSeq, Text}

/** [[XmlJsStream.writeJs]]: a Js's code, written through the same (iterative) stack as XML. */
class XmlJsStreamJsSpec extends AnyFlatSpec {

  /** Records the bytes, and each write's `last`; each write completes at once. */
  private final class RecordingSink extends Content.Sink {
    val bytes = new ByteArrayOutputStream()
    val lasts = collection.mutable.ArrayBuffer[Boolean]()

    override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = {
      while (bb.hasRemaining) bytes.write(bb.get())
      lasts += last
      cb.succeeded()
    }
  }

  private def written(js: Js): String = {
    val sink = new RecordingSink
    XmlJsStream.writeJs(js, sink)
    assert(sink.lasts.lastOption.contains(true) && sink.lasts.init.forall(!_), sink.lasts)
    sink.bytes.toString(UTF_8)
  }

  private def check(js: Js): Unit = {
    val expected = js.cmd
    val out = written(js)
    assert(out == expected, s"\n  written: ${out.take(300)}\n  cmd:     ${expected.take(300)}")
  }

  /** (A Unit lambda: one returning Nothing, as `throw t`, can't be a Consumer's.) */
  private def rethrow(t: Throwable): Unit = throw t

  private val a = Js("a()")
  private val b = Js("b(\"q\")")

  "writeJs" should "write a Js's cmd (as it is: not escaped)" in {
    check(Js("alert('x' < 1 && \"y\" > 2)"))
    check(Js.Void)
    check(Js(""))
    check(Js("é € 😀  "))
  }

  it should "write a CompositeJs's children (e.g. joined, with a separator between them)" in {
    check(Js(a, b))
    check(CompositeJs.joined(Seq(a, b, Js("c()")), Js(";")))
    check(CompositeJs.joined(Seq(a), Js(";")))
    check(CompositeJs.joined(Seq(), Js(";")))
    check(CompositeJs.joined(Seq(a, Js.Void, b), Js(", ")))
    check(CompositeJs.joined(List(a, b, a), Js("|"))) // (joined from a List)
    check(CompositeJs.joined(Seq(a, b), new CompositeJs(Js("/*"), Js("*/")))) // (a composite separator)
  }

  it should "write nested composites, & chains and functions" in {
    check(a & b & Js.Void & a)
    check(Js(Js("["), CompositeJs.joined(Seq(a, CompositeJs.joined(Seq(b, a), Js(";"))), Js(",")), Js("]")))
    check(new JsFunc1(arg => js"f($arg, ${a & b})"))
    check(new JsFunc3((x, y, z) => Js(x, Js(","), y, Js(","), z)))
    check(js"if (x) { $a } else { ${b & a} }")
  }

  it should "write the other kinds of Js as their cmd (e.g. escaped HTML)" in {
    val html = <p class="c" title={"t \"q\""}>{"Hello & <bye> é"}</p>
    check(new EncJs(new NodeSeqJs(html), inString = true))
    check(js"el.innerHTML = ${new EncJs(new NodeSeqJs(html), inString = true)};")
  }

  it should "write a NodeSeqJs's XML, as XML, raw, wherever it is" in {
    val html = <div id="x" class="c"><p title={"t \"q\" & <x>"}>{"Hello & <bye> é € 😀"}</p><br/><!-- c --></div>
    check(new NodeSeqJs(html))
    check(new NodeSeqJs(NodeSeq.Empty))
    check(new NodeSeqJs(html ++ <span>s</span> ++ Text(" t & ")))
    check(js"var tpl = `${new NodeSeqJs(html)}`; ${a & new NodeSeqJs(<b>b</b>)}")
    // Its own namespaces (TopScope), not the ones of the XML it's in.
    val ns = <svg:g xmlns:svg="http://www.w3.org/2000/svg"><svg:rect/></svg:g>
    check(new NodeSeqJs(ns))
    // The same as the XML written by itself (writeSeq).
    val viaWriteSeq = {
      val sink = new RecordingSink
      XmlJsStream.writeSeq(html, sink)
      sink.bytes.toString(UTF_8)
    }
    assert(written(Js(a, new NodeSeqJs(html), b)) == a.cmd + viaWriteSeq + b.cmd)
  }

  it should "write an EncJs with a NodeSeqJs in it escaped (as its cmd), not raw" in {
    val html = <p class="c">{"a \"b\" <c> é \u2028"}</p>
    val enc = new EncJs(new NodeSeqJs(html), inString = true)
    check(enc)
    check(Js(Js("el.innerHTML = "), enc, Js(";")))
    check(new EncJs(Js(Js("x = "), enc))) // escaped twice
    assert(!written(enc).contains("<p"))
  }

  it should "write a NodeSeqJs with a big page in it, pausing as the sink is slow" in {
    val page = <div>{(1 to 20_000).map(i => <p class="row" title={s"r$i"}>{s"Row $i é € 😀 \"q\" & <x>"}</p>)}</div>
    val js = Js(Js("document.body.innerHTML = `"), new NodeSeqJs(page), Js("`;"))
    var pending: Callback = null
    var writes = 0
    val bytes = new ByteArrayOutputStream()
    val sink = new Content.Sink {
      override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = {
        assert(pending == null)
        writes += 1
        while (bb.hasRemaining) bytes.write(bb.get())
        pending = cb
      }
    }
    var done = 0
    XmlJsStream.writeJs(js, sink, Callback.from(() => done += 1, (t: Throwable) => rethrow(t)))
    assert(writes == 1 && done == 0)
    while (pending != null) { val cb = pending; pending = null; cb.succeeded() }
    assert(done == 1 && writes > 10)
    assert(bytes.toString(UTF_8) == js.cmd)
  }

  it should "write very deep composites without overflowing the stack (iteratively)" in {
    val n = 20_000
    val deep = (1 until n).foldLeft(Js("x()"))((acc, _) => acc & Js("x()")) // nested n levels deep, on the left
    val out = written(deep)
    assert(out == Seq.fill(n)("x()").mkString(";"))
  }

  it should "end the content only if endContent" in {
    val sink = new RecordingSink
    var done = false
    XmlJsStream.writeJs(Js(a, b), sink, Callback.from(() => done = true, (t: Throwable) => rethrow(t)), endContent = false)
    assert(done && sink.lasts.forall(!_) && sink.bytes.toString(UTF_8) == a.cmd + b.cmd)
  }

  it should "write an empty Js as one last, empty write" in {
    val sink = new RecordingSink
    XmlJsStream.writeJs(Js.Void, sink)
    assert(sink.lasts.toSeq == Seq(true) && sink.bytes.size() == 0)
  }

  it should "not block: pause while the sink is slow, and go on as its writes complete" in {
    val produced = new AtomicInteger()
    // Many parts, each produced (its cmd built) only when it's written: so how far it got is visible.
    val parts = (1 to 50_000).map(i => new Js {
      override def cmd: String = { produced.incrementAndGet(); s"part($i);" }
    })
    var pending: Callback = null
    val bytes = new ByteArrayOutputStream()
    val sink = new Content.Sink {
      override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = {
        assert(pending == null, "a write while another was in progress")
        while (bb.hasRemaining) bytes.write(bb.get())
        pending = cb
      }
    }
    val completions = new AtomicInteger()
    XmlJsStream.writeJs(Js(parts *), sink, Callback.from(() => { completions.incrementAndGet(); () },(t: Throwable) => rethrow(t)))
    assert(pending != null && completions.get() == 0)
    assert(produced.get() < 10_000, produced.get()) // paused, after a few chunks
    while (pending != null) {
      val cb = pending
      pending = null
      cb.succeeded()
    }
    assert(completions.get() == 1 && produced.get() == 50_000)
    assert(bytes.toString(UTF_8) == (1 to 50_000).map(i => s"part($i);").mkString)
  }

  it should "fail the callback, once, if a write fails, or a Js's cmd throws" in {
    def outcome(js: Js, sink: Content.Sink): Option[Throwable] = {
      var result: Option[Throwable] = null
      var calls = 0
      XmlJsStream.writeJs(js, sink, Callback.from(() => { calls += 1; result = None }, (t: Throwable) => { calls += 1; result = Some(t) }))
      assert(calls == 1)
      result
    }
    val failingSink = new Content.Sink {
      override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = cb.failed(new java.io.IOException("gone"))
    }
    assert(outcome(Js(a, b), failingSink).exists(_.getMessage == "gone"))
    val throwing = new Js {
      override def cmd: String = throw new IllegalStateException("no cmd")
    }
    assert(outcome(Js(a, throwing, b), new RecordingSink).exists(_.getMessage == "no cmd"))
  }
}
