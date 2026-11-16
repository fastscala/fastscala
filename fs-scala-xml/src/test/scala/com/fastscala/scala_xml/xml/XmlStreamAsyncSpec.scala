package com.fastscala.scala_xml.xml

import com.fastscala.scala_xml.xml.streaming.*

import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import org.scalatest.flatspec.AnyFlatSpec

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CountDownLatch, Executors, TimeUnit}
import scala.xml.*

/**
 * [[XmlJsStream]] without blocking: one write to the sink at a time, serializing pausing while the sink is slow (and
 * resumed by the write that drains the queue), the callback completed once, at the end or on the first failure.
 */
class XmlStreamAsyncSpec extends AnyFlatSpec {

  /**
   * Holds each write until it's completed (by the test, or by `completeFrom`'s thread). Its bytes are copied only then,
   * as a network write would send them, so a buffer reused before its write completed would show in the output.
   */
  private final class DeferredSink extends Content.Sink {
    val bytes = new ByteArrayOutputStream()
    private var pending: (Boolean, ByteBuffer, Callback) = null
    var writes = 0
    var lastWritten = false
    var failNext = false

    override def write(last: Boolean, byteBuffer: ByteBuffer, callback: Callback): Unit = synchronized {
      assert(pending == null, "a write while another was in progress")
      assert(!lastWritten, "a write after the last one")
      writes += 1
      pending = (last, byteBuffer, callback)
      notifyAll()
    }

    def hasPending: Boolean = synchronized(pending != null)

    /** Completes the pending write: succeeded (its bytes "sent"), or failed if `failNext`. */
    def completeOne(): Unit = {
      val (last, bb, cb) = synchronized {
        val p = pending
        assert(p != null, "no write to complete")
        if (!failNext) {
          while (p._2.hasRemaining) bytes.write(p._2.get())
          if (p._1) lastWritten = true
        }
        pending = null
        p
      }
      if (failNext) cb.failed(new java.io.IOException("client gone")) else cb.succeeded()
    }

    def asString: String = synchronized(bytes.toString(UTF_8))
  }

  /** Records the completion of the callback (and that there's only one). */
  private final class RecordingCallback extends Callback {
    val done = new CountDownLatch(1)
    val completions = new AtomicInteger()
    @volatile var failure: Option[Throwable] = None

    override def succeeded(): Unit = { completions.incrementAndGet(); done.countDown() }

    override def failed(x: Throwable): Unit = { failure = Some(x); completions.incrementAndGet(); done.countDown() }

    def isDone: Boolean = done.getCount == 0
  }

  /** A page whose children are produced lazily, counting how many have been read (so, serialized). */
  private def lazyPage(rows: Int, read: AtomicInteger): Elem =
    new Elem(null, "div", Null, TopScope, false,
      LazyList.tabulate(rows) { i => read.incrementAndGet(); <p class="row">{s"Row $i é € & <x>"}</p> } *)

  /** The same page, eagerly (to compare the output with). */
  private def eagerPage(rows: Int): Elem =
    <div>{(0 until rows).map(i => <p class="row">{s"Row $i é € & <x>"}</p>)}</div>

  "XmlJsStream (not blocking)" should "return at once, pause when the sink is slow, and go on as writes complete" in {
    val rows = 50_000 // ~2 MB: hundreds of chunks
    val read = new AtomicInteger()
    val sink = new DeferredSink
    val cb = new RecordingCallback
    XmlJsStream.writeSeq(lazyPage(rows, read), sink, "<!DOCTYPE html>\n", cb)

    // Returned with one write in progress and serializing paused: only a few chunks' worth of rows read.
    assert(sink.writes == 1 && sink.hasPending && !cb.isDone)
    val rowsPerChunk = XmlJsStream.ChunkSize / eagerPage(1).child.head.toString.getBytes(UTF_8).length
    val readWhenPaused = read.get()
    assert(readWhenPaused > 0 && readWhenPaused <= (XmlJsStream.MaxQueuedChunks + 3) * rowsPerChunk, (readWhenPaused, rowsPerChunk))

    // Completing writes lets it go on (resumed by the writes), until the end: then the callback, once.
    var completedWrites = 0
    while (sink.hasPending) {
      sink.completeOne()
      completedWrites += 1
      assert(read.get() <= rows)
    }
    assert(cb.isDone && cb.completions.get() == 1 && cb.failure.isEmpty)
    assert(read.get() == rows && completedWrites == sink.writes && completedWrites > 100)
    assert(sink.lastWritten)
    val same = sink.asString == "<!DOCTYPE html>\n" + eagerPage(rows).toString
    assert(same, "output differs")
  }

  it should "write correctly when the writes complete on another thread, at any time" in {
    val pool = Executors.newSingleThreadExecutor()
    try {
      for (round <- 1 to 5) {
        val rows = 10_000 * round
        val sink = new DeferredSink
        val cb = new RecordingCallback
        val rnd = new scala.util.Random(round)
        // The "network": completes the pending writes, after random short pauses.
        val network = pool.submit(new Runnable {
          override def run(): Unit = while (!cb.isDone) {
            if (sink.hasPending) {
              if (rnd.nextInt(4) == 0) Thread.sleep(0, rnd.nextInt(200_000))
              sink.completeOne()
            } else sink.synchronized(sink.wait(1))
          }
        })
        XmlJsStream.writeSeq(lazyPage(rows, new AtomicInteger()), sink, "", cb)
        assert(cb.done.await(30, TimeUnit.SECONDS), "not completed")
        network.get(10, TimeUnit.SECONDS)
        assert(cb.completions.get() == 1 && cb.failure.isEmpty, cb.failure)
        val same = sink.asString == eagerPage(rows).toString
        assert(same, s"output differs (round $round)")
      }
    } finally pool.shutdownNow()
  }

  it should "fail the callback, once, if a write fails, and write nothing more" in {
    val sink = new DeferredSink
    val cb = new RecordingCallback
    XmlJsStream.writeSeq(eagerPage(20_000), sink, "", cb)
    sink.completeOne()
    sink.completeOne()
    sink.failNext = true
    sink.completeOne() // the third write fails
    assert(cb.isDone && cb.completions.get() == 1)
    assert(cb.failure.exists(_.getMessage == "client gone"))
    assert(!sink.hasPending && sink.writes == 3, sink.writes)
  }

  it should "fail the callback, once, if serializing fails midway (after some writes)" in {
    val custom = new Node {
      override def label: String = "custom"
      override def child: Seq[Node] = Nil
    }
    val sink = new DeferredSink
    val cb = new RecordingCallback
    val page = <div>{(1 to 3_000).map(i => <p>{s"Row $i"}</p>)}{custom}<p>never written</p></div>
    XmlJsStream.writeSeq(page, sink, "", cb)
    while (sink.hasPending && !cb.isDone) sink.completeOne()
    assert(cb.isDone && cb.completions.get() == 1)
    assert(cb.failure.exists(_.isInstanceOf[IllegalArgumentException]), cb.failure)
    if (sink.hasPending) sink.completeOne() // a write still in progress when it failed: completes, nothing follows
    assert(!sink.hasPending && !sink.lastWritten && !sink.asString.contains("never written"))
  }

  it should "end an empty page with one last write, and then complete the callback" in {
    val sink = new DeferredSink
    val cb = new RecordingCallback
    XmlJsStream.writeSeq(NodeSeq.Empty, sink, "", cb)
    assert(sink.writes == 1 && !cb.isDone)
    sink.completeOne()
    assert(cb.isDone && cb.failure.isEmpty && sink.lastWritten && sink.asString == "")
  }
}
