package com.fastscala.scala_xml.xml.streaming

import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.Callback

import java.nio.ByteBuffer

/**
 * Writes what a producer puts into byte buffers to a Jetty [[Content.Sink]], without blocking, while the producer goes
 * on filling the next buffer.
 *
 * The producer fills `buf` (from `pos` on, as an [[EncoderOutput]]); each full buffer is queued (by [[flush]]) and
 * a new one taken; the last one is queued by [[finish]], and written with `last = true` (it ends the content, if
 * `endContent`). Buffers are written one at a time, in order (as Jetty requires), each one as the previous write
 * completes, on the thread that completed it.
 *
 * The producer is [[produce]], run by [[drive]]: it should check `checkPause` regularly (cheap: a field), and when it's
 * set, call [[shouldPause]], and if that's true return false (paused). Once the queue is down again (to
 * [[NonBlockingChunkedSinkWriter.ResumeAtQueued]]), the write that drained it calls [[drive]] again, on its thread: the producer
 * goes on from where it stopped. So a slow client doesn't hold a thread, and only a few buffers are kept: more only
 * while the producer doesn't pause (e.g. for a single node bigger than that).
 *
 * `callback` is completed once: after the last write completed, or with the first failure (the producer's, or a
 * write's: then nothing more is written, and the producer, at its next check, pauses for good).
 */
private[xml] trait NonBlockingChunkedSinkWriter extends EncoderOutput {

  import NonBlockingChunkedSinkWriter.*

  /** Where the buffers are written. */
  protected def sink: Content.Sink

  /** Completed once, when everything is written (or on the first failure). */
  protected def callback: Callback

  /** Whether the last write ends the content. */
  protected def endContent: Boolean

  /**
   * Produces the output (into `buf` and `pos`, calling [[flush]] when `buf` is full), from where it stopped: true once
   * it's all produced, false if it paused (see [[shouldPause]]). It's resumed (called again) by [[drive]].
   */
  protected def produce(): Boolean

  var buf: Array[Byte] = new Array[Byte](ChunkSize)
  var pos: Int = 0

  /** Set when the producer should call [[shouldPause]]: the queue is full, or there was a failure. */
  var checkPause: Boolean = false

  private val lock = new Object
  private val queued = new java.util.ArrayDeque[Chunk]()
  private val freeBuffers = new java.util.ArrayDeque[Array[Byte]]()
  private var writing = false
  private var paused = false
  private var failure: Throwable = null
  private var completed = false

  /** Runs (or resumes) the producer: when it has produced everything, queues the last buffer. */
  def drive(): Unit =
    try {
      if (produce()) finish()
    } catch {
      case t: Throwable => fail(t)
    }

  /** Queues the buffer (if anything is in it), to be written, and takes a new one. */
  def flush(): Unit = if (pos > 0) queue(last = false)

  /** Queues the buffer as the last one (even empty: it ends the content). */
  def finish(): Unit = queue(last = true)

  /** Whether the producer should pause now (if so, it's resumed by the write that drains the queue), or stop (a failure). */
  def shouldPause(): Boolean = lock.synchronized {
    checkPause = false
    if (failure ne null) true
    else if (queued.size >= MaxQueued) {
      paused = true
      true
    } else false
  }

  /** Fails the callback (once), and stops writing (and producing, at the producer's next check). */
  def fail(t: Throwable): Unit = {
    lock.synchronized {
      if (failure eq null) failure = t
      checkPause = true
    }
    complete(t)
  }

  private def queue(last: Boolean): Unit = {
    val chunk = new Chunk(buf, pos, last, endsContent = last && endContent)
    lock.synchronized {
      queued.add(chunk)
      if (!last) {
        val free = freeBuffers.poll()
        buf = if (free ne null) free else new Array[Byte](ChunkSize)
      }
      if (queued.size >= MaxQueued || (failure ne null)) checkPause = true
    }
    pos = 0
    writeNext()
  }

  private def writeNext(): Unit = {
    var next: Chunk = null
    lock.synchronized {
      if (!writing && (failure eq null) && !queued.isEmpty) {
        writing = true
        next = queued.poll()
      }
    }
    if (next ne null) {
      val chunk = next
      sink.write(chunk.endsContent, ByteBuffer.wrap(chunk.bytes, 0, chunk.length), new Callback {
        override def succeeded(): Unit = written(chunk, null)

        override def failed(t: Throwable): Unit = written(chunk, t)
      })
    }
  }

  private def written(chunk: Chunk, t: Throwable): Unit = {
    var resume = false
    lock.synchronized {
      writing = false
      if (chunk.bytes.length == ChunkSize && freeBuffers.size < MaxFreeBuffers) freeBuffers.add(chunk.bytes)
      if ((t ne null) && (failure eq null)) failure = t
      if ((failure eq null) && paused && queued.size <= ResumeAtQueued) {
        paused = false
        resume = true
      }
    }
    if (t ne null) fail(t)
    else if (chunk.last) complete(null)
    else {
      // The next write starts here, on the thread that completed this one: if the sink throws (instead of failing
      // the write's callback), it's our failure, not that thread's (the callback would never be completed).
      val started =
        try {
          writeNext()
          true
        } catch {
          case t: Throwable =>
            fail(t)
            false
        }
      if (started && resume) drive()
    }
  }

  private def complete(t: Throwable): Unit = {
    val first = lock.synchronized {
      val first = !completed
      completed = true
      first
    }
    if (first) {
      if (t eq null) callback.succeeded() else callback.failed(t)
    }
  }
}

private[xml] object NonBlockingChunkedSinkWriter {

  /** The size of each buffer (each write, but the last). */
  val ChunkSize = 8192

  /** The producer is asked to pause once this many buffers are waiting to be written (besides the one being written)... */
  val MaxQueued = 3

  /** ... and resumed once they're down to this many. */
  final val ResumeAtQueued = 1

  /**
   * At most this many written buffers are kept for reuse: as many as can be in use at once otherwise (the queued ones
   * and the one being written). More are only allocated while the producer doesn't pause; once written, they're
   * dropped (left to the GC), instead of being kept until the end of the response.
   */
  final val MaxFreeBuffers = MaxQueued + 1

  private final class Chunk(val bytes: Array[Byte], val length: Int, val last: Boolean, val endsContent: Boolean)
}
