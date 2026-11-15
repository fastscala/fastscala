package com.fastscala.scala_xml.xml

import com.fastscala.js.{EncJs, Js}
import com.fastscala.scala_xml.js.JS
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import org.scalatest.flatspec.AnyFlatSpec

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.atomic.AtomicInteger
import scala.xml.*

/**
 * How XmlJsStream hands its chunks to the sink (besides [[XmlStreamAsyncSpec]]): whether the content is ended, the
 * chunks of a single node bigger than the queue, failures while paused, and sinks that throw instead of failing.
 */
class XmlJsStreamFlowSpec extends AnyFlatSpec {

  /** Holds each write until the test completes it; copies its bytes only then (a reused buffer would show). */
  private final class HeldSink extends Content.Sink {
    val bytes = new ByteArrayOutputStream()
    val lasts = collection.mutable.ArrayBuffer[Boolean]()
    private var pending: (ByteBuffer, Callback) = null
    var throwOnWrite: Int = -1 // the number of the write (from 1) that throws, instead of being held

    override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = synchronized {
      assert(pending == null, "a write while another was in progress")
      lasts += last
      if (lasts.size == throwOnWrite) throw new IllegalStateException("sink broken")
      pending = (bb, cb)
    }

    def hasPending: Boolean = synchronized(pending != null)

    def complete(fail: Boolean = false): Unit = {
      val (bb, cb) = synchronized {
        val p = pending
        assert(p != null, "no write to complete")
        if (!fail) while (p._1.hasRemaining) bytes.write(p._1.get())
        pending = null
        p
      }
      if (fail) cb.failed(new java.io.IOException("gone")) else cb.succeeded()
    }

    def completeAll(): Unit = while (hasPending) complete()

    def text: String = bytes.toString(UTF_8)
  }

  private final class Recording extends Callback {
    val completions = new AtomicInteger()
    @volatile var failure: Option[Throwable] = None

    override def succeeded(): Unit = completions.incrementAndGet()

    override def failed(x: Throwable): Unit = { failure = Some(x); completions.incrementAndGet() }
  }

  private def lazyRows(rows: Int, read: AtomicInteger): Elem =
    new Elem(null, "div", Null, TopScope, false, LazyList.tabulate(rows) { i => read.incrementAndGet(); <p>{s"Row $i é"}</p> } *)

  "writeSeq with endContent = false" should "write everything, without ending the content (no write with last = true)" in {
    val sink = new HeldSink
    val cb = new Recording
    val page = <div>{(1 to 2_000).map(i => <p>{s"Row $i"}</p>)}</div>
    XmlJsStream.writeSeq(page, sink, "<!-- x -->", cb, endContent = false)
    sink.completeAll()
    assert(cb.completions.get() == 1 && cb.failure.isEmpty)
    assert(sink.lasts.forall(!_), sink.lasts)
    assert(sink.text == "<!-- x -->" + page.toString)
  }

  "writeJs with endContent = false" should "write the code, without ending the content" in {
    val sink = new HeldSink
    val cb = new Recording
    val js = JS.htmlAsJsStr(<div>{(1 to 2_000).map(i => <p>{s"Row $i"}</p>)}</div>)
    XmlJsStream.writeJs(js, sink, cb, endContent = false)
    sink.completeAll()
    assert(cb.completions.get() == 1 && cb.failure.isEmpty)
    assert(sink.lasts.forall(!_))
    assert(sink.text == js.cmd)
  }

  "writeJs" should "write the code asynchronously, ending the content" in {
    val sink = new HeldSink
    val cb = new Recording
    val js = Js(Js("f("), new EncJs(Js("a \"b\" " * 5_000), inString = true), Js(");"))
    XmlJsStream.writeJs(js, sink, cb)
    assert(sink.hasPending && cb.completions.get() == 0)
    sink.completeAll()
    assert(cb.completions.get() == 1 && cb.failure.isEmpty)
    assert(sink.lasts.last && sink.lasts.init.forall(!_))
    assert(sink.text == js.cmd)
  }

  "A single node bigger than the queue" should "be written whole (serializing only pauses between nodes), in chunks" in {
    val big = "é<>&\"" * 200_000 // a few MB, in a single Text
    val sink = new HeldSink
    val cb = new Recording
    XmlJsStream.writeNode(<p title="t">{big}</p>, sink, cb)
    val chunks = sink.lasts.size
    sink.completeAll()
    assert(cb.completions.get() == 1 && cb.failure.isEmpty)
    assert(sink.text == (<p title="t">{big}</p>).toString)
    assert(sink.lasts.size > XmlJsStream.MaxQueuedChunks + 1 && chunks == 1)
  }

  "A failed write, while paused" should "fail the callback once, and not resume serializing" in {
    val read = new AtomicInteger()
    val sink = new HeldSink
    val cb = new Recording
    XmlJsStream.writeSeq(lazyRows(100_000, read), sink, "", cb)
    val readWhenPaused = read.get()
    sink.complete(fail = true)
    assert(cb.completions.get() == 1 && cb.failure.exists(_.getMessage == "gone"))
    assert(!sink.hasPending, "a write after the failure")
    assert(read.get() == readWhenPaused, "serializing went on after the failure")
  }

  "A sink whose first write throws" should "fail the callback, once" in {
    val sink = new HeldSink
    sink.throwOnWrite = 1
    val cb = new Recording
    XmlJsStream.writeSeq(<div>{(1 to 5_000).map(i => <p>{s"Row $i"}</p>)}</div>, sink, "", cb)
    assert(cb.completions.get() == 1 && cb.failure.exists(_.isInstanceOf[IllegalStateException]), cb.failure)
  }

  "A sink whose write throws when called from the completion of the previous one" should "fail the callback, once" in {
    // Regression (fixed): the next write is started from the previous one's callback (Out.written -> writeNext), where a throwing
    // sink.write isn't caught: the exception goes to whoever completed the previous write, and the callback is never
    // completed (the response hangs).
    val sink = new HeldSink
    sink.throwOnWrite = 2
    val cb = new Recording
    XmlJsStream.writeSeq(<div>{(1 to 5_000).map(i => <p>{s"Row $i"}</p>)}</div>, sink, "", cb)
    try sink.complete() catch { case _: IllegalStateException => } // (the exception escapes into the completer)
    assert(cb.completions.get() == 1 && cb.failure.exists(_.isInstanceOf[IllegalStateException]), cb.failure)
  }

  "The callback" should "be completed only after the last write has completed" in {
    val sink = new HeldSink
    val cb = new Recording
    XmlJsStream.writeSeq(<div>{(1 to 3_000).map(i => <p>{s"Row $i"}</p>)}</div>, sink, "", cb)
    while (sink.hasPending) {
      assert(cb.completions.get() == 0)
      sink.complete()
    }
    assert(cb.completions.get() == 1)
  }

  "Writes completed inside sink.write (synchronously)" should "write everything, in order, completing the callback once" in {
    val bytes = new ByteArrayOutputStream()
    var depth = 0
    var maxDepth = 0
    val sink = new Content.Sink {
      override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = {
        depth += 1
        maxDepth = math.max(maxDepth, depth)
        while (bb.hasRemaining) bytes.write(bb.get())
        cb.succeeded()
        depth -= 1
      }
    }
    val cb = new Recording
    val page = <div>{(1 to 20_000).map(i => <p>{s"Row $i é"}</p>)}</div>
    XmlJsStream.writeSeq(page, sink, "", cb)
    assert(cb.completions.get() == 1 && cb.failure.isEmpty)
    assert(bytes.toString(UTF_8) == page.toString)
    assert(maxDepth <= 2, s"writes nested $maxDepth deep (each completion starting the next write inside the previous one)")
  }
}
