package com.fastscala.scala_xml.xml

import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback
import org.scalatest.flatspec.AnyFlatSpec

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CountDownLatch, Executors, TimeUnit}

/**
 * [[ChunkedSinkWriter]] by itself, with a producer of bytes (not XML): writes in order, one at a time, pausing while
 * the sink is slow and resumed by the writes, and failing once.
 */
class ChunkedSinkWriterSpec extends AnyFlatSpec {

  import ChunkedSinkWriter.{ChunkSize, MaxQueued}

  /** The byte at offset `i` of what the producer writes. */
  private def byteAt(i: Int): Byte = (i * 31 + i / 7).toByte

  private def expected(n: Int): Array[Byte] = Array.tabulate(n)(byteAt)

  /** Writes `total` bytes, in "items" of `itemSize` (it only pauses between items, as XmlJsStream between nodes). */
  private final class Producer(total: Int, protected val sink: Content.Sink, protected val callback: Callback, protected val endContent: Boolean = true,
                               itemSize: Int = 100, failAt: Int = -1) extends ChunkedSinkWriter {
    @volatile var produced = 0
    @volatile var produceCalls = 0

    override protected def produce(): Boolean = {
      produceCalls += 1
      while (produced < total) {
        if (checkPause && shouldPause()) return false
        if (produced == failAt) throw new IllegalStateException("producer failed")
        val end = math.min(total, produced + itemSize)
        while (produced < end) {
          if (pos == buf.length) flush()
          buf(pos) = byteAt(produced)
          pos += 1
          produced += 1
        }
      }
      true
    }
  }

  /** Holds each write until the test completes it; copies its bytes only then (a buffer reused too soon would show). */
  private final class HeldSink extends Content.Sink {
    val bytes = new ByteArrayOutputStream()
    val lasts = collection.mutable.ArrayBuffer[Boolean]()
    val sizes = collection.mutable.ArrayBuffer[Int]()
    private var pending: (ByteBuffer, Callback) = null

    override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = synchronized {
      assert(pending == null, "a write while another was in progress")
      lasts += last
      sizes += bb.remaining()
      pending = (bb, cb)
      notifyAll()
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
  }

  private final class Recording extends Callback {
    val done = new CountDownLatch(1)
    val completions = new AtomicInteger()
    @volatile var failure: Option[Throwable] = None

    override def succeeded(): Unit = { completions.incrementAndGet(); done.countDown() }

    override def failed(x: Throwable): Unit = { failure = Some(x); completions.incrementAndGet(); done.countDown() }
  }

  "ChunkedSinkWriter" should "write everything, in order, in buffers of ChunkSize, the last one ending the content" in {
    val sink = new HeldSink
    val cb = new Recording
    val total = ChunkSize * 10 + 123
    new Producer(total, sink, cb).drive()
    sink.completeAll()
    assert(cb.completions.get() == 1 && cb.failure.isEmpty)
    assert(java.util.Arrays.equals(sink.bytes.toByteArray, expected(total)))
    assert(sink.sizes.init.forall(_ == ChunkSize) && sink.sizes.last == 123)
    assert(sink.lasts.last && sink.lasts.init.forall(!_))
  }

  it should "write a single last write when everything fits in one buffer, and an empty one for nothing" in {
    for (total <- Seq(0, 1, ChunkSize - 1)) {
      val sink = new HeldSink
      val cb = new Recording
      new Producer(total, sink, cb).drive()
      assert(sink.lasts == Seq(true) && sink.sizes == Seq(total), total)
      sink.complete()
      assert(cb.completions.get() == 1)
    }
  }

  it should "not end the content with endContent = false" in {
    val sink = new HeldSink
    val cb = new Recording
    new Producer(ChunkSize * 3, sink, cb, endContent = false).drive()
    sink.completeAll()
    assert(sink.lasts.forall(!_) && cb.completions.get() == 1)
  }

  it should "pause the producer once MaxQueued buffers wait, and resume it as the writes drain them" in {
    val sink = new HeldSink
    val cb = new Recording
    val total = ChunkSize * 50
    val p = new Producer(total, sink, cb)
    p.drive()
    // One being written, MaxQueued queued, and the one being filled (an item over, at most).
    assert(p.produced <= (MaxQueued + 2) * ChunkSize + 100, p.produced)
    assert(p.produced >= MaxQueued * ChunkSize, p.produced)
    assert(sink.lasts.size == 1 && cb.completions.get() == 0)
    sink.completeAll()
    assert(p.produced == total && p.produceCalls > 10, p.produceCalls)
    assert(java.util.Arrays.equals(sink.bytes.toByteArray, expected(total)))
    assert(cb.completions.get() == 1)
  }

  it should "not pause inside an item (an item bigger than the queue is written whole)" in {
    val sink = new HeldSink
    val cb = new Recording
    val total = ChunkSize * 20
    val p = new Producer(total, sink, cb, itemSize = total)
    p.drive()
    assert(p.produced == total && p.produceCalls == 1)
    sink.completeAll()
    assert(java.util.Arrays.equals(sink.bytes.toByteArray, expected(total)))
  }

  it should "fail the callback once, and write and produce nothing more, when a write fails" in {
    val sink = new HeldSink
    val cb = new Recording
    val p = new Producer(ChunkSize * 50, sink, cb)
    p.drive()
    sink.complete()
    val producedBefore = p.produced
    sink.complete(fail = true)
    assert(cb.completions.get() == 1 && cb.failure.exists(_.getMessage == "gone"))
    assert(!sink.hasPending)
    assert(p.produced <= producedBefore + 100, "the producer went on after the failure")
  }

  it should "fail the callback once when the producer throws (after writes)" in {
    val sink = new HeldSink
    val cb = new Recording
    val p = new Producer(ChunkSize * 50, sink, cb, failAt = 20_000) // (an item boundary: past 2 buffers, before the pause)
    p.drive()
    while (sink.hasPending && cb.completions.get() == 0) sink.complete()
    assert(cb.completions.get() == 1 && cb.failure.exists(_.getMessage == "producer failed"), cb.failure)
    if (sink.hasPending) sink.complete()
    assert(!sink.lasts.contains(true))
  }

  it should "fail the callback once when the sink throws, on the producer's thread or on a completion's" in {
    for (throwAt <- Seq(1, 2, 3)) {
      var writes = 0
      var pending: Callback = null
      val sink = new Content.Sink {
        override def write(last: Boolean, bb: ByteBuffer, cb: Callback): Unit = {
          writes += 1
          if (writes == throwAt) throw new IllegalStateException("sink broken")
          pending = cb
        }
      }
      val cb = new Recording
      new Producer(ChunkSize * 20, sink, cb).drive()
      while (pending != null && cb.completions.get() == 0) {
        val p = pending
        pending = null
        p.succeeded()
      }
      assert(cb.completions.get() == 1 && cb.failure.exists(_.getMessage == "sink broken"), s"throwAt $throwAt: ${cb.failure}")
    }
  }

  it should "write correctly when the writes complete on another thread, at any time" in {
    val pool = Executors.newSingleThreadExecutor()
    try {
      for (round <- 1 to 5) {
        val sink = new HeldSink
        val cb = new Recording
        val rnd = new scala.util.Random(round)
        val total = ChunkSize * 40 * round + round
        val network = pool.submit(new Runnable {
          override def run(): Unit = while (cb.done.getCount > 0) {
            if (sink.hasPending) {
              if (rnd.nextInt(4) == 0) Thread.sleep(0, rnd.nextInt(200_000))
              sink.complete()
            } else sink.synchronized(sink.wait(1))
          }
        })
        new Producer(total, sink, cb, itemSize = 1 + rnd.nextInt(3000)).drive()
        assert(cb.done.await(30, TimeUnit.SECONDS), s"round $round: not completed")
        network.get(10, TimeUnit.SECONDS)
        assert(cb.completions.get() == 1 && cb.failure.isEmpty, cb.failure)
        assert(java.util.Arrays.equals(sink.bytes.toByteArray, expected(total)), s"round $round: output differs")
      }
    } finally pool.shutdownNow()
  }
}
