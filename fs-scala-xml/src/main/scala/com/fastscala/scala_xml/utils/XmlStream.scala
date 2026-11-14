package com.fastscala.scala_xml.utils

import com.fastscala.js.Js
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.{Blocker, Callback}

import java.nio.ByteBuffer
import java.util.Arrays
import scala.annotation.{nowarn, tailrec}
import scala.collection.immutable.IndexedSeq
import scala.xml.*

/**
 * Writes XML, as `Node.toString` would (attributes of an [[ElemOptimized]] in its own order), encoded in UTF-8,
 * straight into a Jetty [[Content.Sink]] (e.g. a Response): no String, char[] or encoder in between.
 *
 * Output goes into `byte[]` chunks of [[ChunkSize]] bytes (see [[Out]]), each written to the sink once full; the last
 * one is written with `last = true`, so the content ends with it (a page that fits in one chunk is a single write).
 * Most of a page is ASCII (Java stores it as Latin-1 bytes): ASCII runs are copied as they are, in bulk; anything else
 * is encoded to UTF-8 char by char.
 *
 * Writing doesn't block (with a callback: see [[writeSeq]]): one write to the sink at a time (as Jetty requires), while
 * serializing goes on into the next chunk; once [[MaxQueuedChunks]] chunks are waiting for their turn, serializing
 * pauses (between nodes), and the write that drains the queue resumes it, on its thread. So a slow client doesn't hold
 * a thread, and only a few chunks are kept (more only for a single node bigger than that, e.g. a huge text).
 */
object XmlStream {

  val ChunkSize = 8192

  // The special values of Out.indexStack (a value >= 0 is the cursor into an indexed seq):
  /** The level's nodes are a non-empty `::` (List), read head by head: nodesStack holds the remaining list. */
  private final val ListCursor = -1
  /** The level's nodes are read with an Iterator (any other kind of seq): nodesStack holds the iterator. */
  private final val IteratorCursor = -2
  /** The level is a single node (writeNode's root, or a Node used as its own child seq): nodesStack holds the node. */
  private final val SingleNodeCursor = -3

  /** Strings (and runs) up to this long are copied char by char: for a few bytes, a String.getBytes call costs more. */
  private final val ShortString = 32

  /** Serializing pauses once this many chunks are waiting to be written (besides the one being written)... */
  val MaxQueuedChunks = 3

  /** ... and resumes once they're down to this many. */
  private final val ResumeAtQueuedChunks = 1

  /**
   * At most this many written chunks' buffers are kept for reuse: as many as can be in use at once otherwise (the
   * queued ones and the one being written). More are only allocated while a single node bigger than that is written
   * (serializing doesn't pause inside a node); once written, they're dropped (left to the GC), instead of being kept
   * until the end of the response.
   */
  private final val MaxFreeBuffers = MaxQueuedChunks + 1

  /**
   * Writes `prefix` (e.g. a doctype; it isn't escaped) and then `ns`, and ends the sink's content, without blocking:
   * returns at once (possibly having written everything, if the sink takes the writes at once), and completes
   * `callback` once, after the last write (or fails it, on the first failure: a write's, or serializing's).
   */
  def writeSeq(ns: NodeSeq, sink: Content.Sink, prefix: String, callback: Callback): Unit = {
    val out = new Out(sink, callback)
    try {
      out.append(prefix)
      out.pushSeq(ns, TopScope, null)
    } catch {
      case t: Throwable => out.fail(t)
    }
    out.drive()
  }

  /** As [[writeSeq]], for one node. */
  def writeNode(n: Node, sink: Content.Sink, callback: Callback): Unit = {
    val out = new Out(sink, callback)
    out.pushSingle(n, TopScope)
    out.drive()
  }

  /** As the non-blocking [[writeSeq]], but returns once everything is written, and throws if writing fails. */
  def writeSeq(ns: NodeSeq, sink: Content.Sink, prefix: String = ""): Unit =
    blocking(writeSeq(ns, sink, prefix, _))

  /** As the non-blocking [[writeNode]], but returns once everything is written, and throws if writing fails. */
  def writeNode(n: Node, sink: Content.Sink): Unit = blocking(writeNode(n, sink, _))

  private def blocking(write: Callback => Unit): Unit = {
    val callback = Blocker.callback()
    try {
      write(callback)
      callback.block()
    } finally callback.close()
  }

  /**
   * Encodes `s(from until to)` to UTF-8 into `buf` at `p`, returning the position after it. The caller guarantees
   * room for 3 bytes per char (a surrogate pair is 4 bytes for 2 chars). A lone surrogate is written as `?`, as
   * String.getBytes(UTF_8) and the JDK's UTF-8 encoder do.
   */
  private[utils] def encodeUtf8(s: String, from: Int, to: Int, buf: Array[Byte], pos: Int): Int = {
    var p = pos
    var i = from
    while (i < to) {
      val c = s.charAt(i)
      if (c < 0x80) {
        buf(p) = c.toByte
        p += 1
      } else if (c < 0x800) {
        buf(p) = (0xC0 | (c >> 6)).toByte
        buf(p + 1) = (0x80 | (c & 0x3F)).toByte
        p += 2
      } else if (Character.isSurrogate(c)) {
        if (Character.isHighSurrogate(c) && i + 1 < to && Character.isLowSurrogate(s.charAt(i + 1))) {
          val cp = Character.toCodePoint(c, s.charAt(i + 1))
          buf(p) = (0xF0 | (cp >> 18)).toByte
          buf(p + 1) = (0x80 | ((cp >> 12) & 0x3F)).toByte
          buf(p + 2) = (0x80 | ((cp >> 6) & 0x3F)).toByte
          buf(p + 3) = (0x80 | (cp & 0x3F)).toByte
          p += 4
          i += 1
        } else {
          buf(p) = '?'
          p += 1
        }
      } else {
        buf(p) = (0xE0 | (c >> 12)).toByte
        buf(p + 1) = (0x80 | ((c >> 6) & 0x3F)).toByte
        buf(p + 2) = (0x80 | (c & 0x3F)).toByte
        p += 3
      }
      i += 1
    }
    p
  }

  /**
   * Copies `s(from until to)`, which must be all ASCII, into `buf` at `p`: for a Latin-1 String this is an array copy
   * of its bytes. (String.getBytes(int, int, byte[], int) keeps the low 8 bits of each char: deprecated, as that's
   * only right for ASCII, which is all it's used for here.)
   */
  @nowarn("cat=deprecation")
  private inline def copyAscii(s: String, from: Int, to: Int, buf: Array[Byte], p: Int): Unit =
    s.getBytes(from, to, buf, p)

  /** A filled chunk, waiting to be written (or being written). */
  private final class Chunk(val bytes: Array[Byte], val length: Int, val last: Boolean)

  /**
   * The output, and the state of one write. Serializing fills `buf` (a `byte[]` of [[ChunkSize]] bytes); when it's
   * full, [[flush]] queues it, to be written, and serializing goes on in a free (or new) buffer. The chunks are written
   * to the sink one at a time, in order; each written one's buffer is reused. [[finish]] queues the last one.
   *
   * Only one thread serializes at a time (whichever calls [[drive]]: the caller, then the write that resumes it), so
   * the serializing state (`buf`, `pos`, the stacks) isn't locked; the chunks' state (queue, write in progress, pause,
   * failure) is shared with the writes' callbacks, so it is (with `lock`). (`buf` and `pos` are public so the inline
   * methods use them directly.)
   */
  private[utils] final class Out(sink: Content.Sink, callback: Callback) {
    var buf = new Array[Byte](ChunkSize)
    var pos = 0

    private val lock = new Object
    private val queued = new java.util.ArrayDeque[Chunk]()
    private val freeBuffers = new java.util.ArrayDeque[Array[Byte]]()
    /** A write to the sink is in progress (Jetty allows one at a time). */
    private var writing = false
    /** Serializing is paused (waiting for the queue to drain): the write that drains it resumes it. */
    private var paused = false
    /** The first failure (then nothing more is written). */
    private var failure: Throwable = null
    /** The callback was completed. */
    private var completed = false

    /**
     * Set by [[flush]] when serializing should check whether to pause (many chunks queued, or a failure): checked by
     * the serializing thread only, between nodes, so it's a plain field (and the check itself is locked).
     */
    var checkPause = false

    private var _scratch: StringBuilder = null

    def scratch: StringBuilder = {
      if (_scratch eq null) _scratch = new StringBuilder(256)
      _scratch
    }

    // Parallel stack arrays: zero Level object allocations, O(N) for both IndexedSeq and List.
    // indexStack: >= 0, the cursor into an indexed seq; or ListCursor, IteratorCursor or SingleNodeCursor.
    var depth = 0
    var nodesStack = new Array[AnyRef](64)
    var indexStack = new Array[Int](64)
    /** For an indexed level: its length (read once, instead of calling length through the Seq interface per node). */
    var lengthStack = new Array[Int](64)
    var scopeStack = new Array[NamespaceBinding](64)
    var closingStack = new Array[Elem](64)

    /** Serializes (from where it stopped) until done, or until it should pause; then queues the last chunk. */
    def drive(): Unit =
      try {
        if (writeAll(this)) finish()
      } catch {
        case t: Throwable => fail(t)
      }

    /** Queues what's in the buffer (if anything) to be written, not ending the content; goes on in another buffer. */
    def flush(): Unit = if (pos > 0) queue(last = false)

    /** Queues what's in the buffer (even nothing) as the last content. */
    def finish(): Unit = queue(last = true)

    private def queue(last: Boolean): Unit = {
      val chunk = new Chunk(buf, pos, last)
      lock.synchronized {
        queued.add(chunk)
        if (!last) {
          val free = freeBuffers.poll()
          buf = if (free ne null) free else new Array[Byte](ChunkSize)
        }
        if (queued.size >= MaxQueuedChunks || (failure ne null)) checkPause = true
      }
      pos = 0
      writeNext()
    }

    /**
     * Whether serializing should stop here (between nodes): it pauses, if [[MaxQueuedChunks]] are still queued (the
     * write that drains them resumes it), or stops, after a failure.
     */
    def shouldPause(): Boolean = lock.synchronized {
      checkPause = false
      if (failure ne null) true
      else if (queued.size >= MaxQueuedChunks) {
        paused = true
        true
      } else false
    }

    /** Starts writing the next queued chunk, unless a write is in progress (or after a failure). */
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
        sink.write(chunk.last, ByteBuffer.wrap(chunk.bytes, 0, chunk.length), new Callback {
          override def succeeded(): Unit = written(chunk, null)

          override def failed(t: Throwable): Unit = written(chunk, t)
        })
      }
    }

    /**
     * A chunk was written (or failed): writes the next one, and resumes serializing if it's paused and the queue is
     * short again; after the last one, or a failure, completes the callback. (When the sink completes writes at once,
     * this runs inside [[writeNext]]: the nesting is at most as deep as the queue is long.)
     */
    private def written(chunk: Chunk, t: Throwable): Unit = {
      var resume = false
      lock.synchronized {
        writing = false
        if (chunk.bytes.length == ChunkSize && freeBuffers.size < MaxFreeBuffers) freeBuffers.add(chunk.bytes)
        if ((t ne null) && (failure eq null)) failure = t
        if ((failure eq null) && paused && queued.size <= ResumeAtQueuedChunks) {
          paused = false
          resume = true
        }
      }
      if (t ne null) fail(t)
      else if (chunk.last) complete(null)
      else {
        writeNext()
        if (resume) drive()
      }
    }

    /** Fails the write (once): nothing more is written. */
    def fail(t: Throwable): Unit = {
      lock.synchronized {
        if (failure eq null) failure = t
        checkPause = true
      }
      complete(t)
    }

    /** Completes the callback, once: succeeded (`t` null) or failed. */
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

    /** An ASCII char (the markup's own: `<`, `>`, `/`, `=`, `"`, ` `, `:`). */
    inline def append(c: Char): Unit = {
      if (pos == ChunkSize) flush()
      buf(pos) = c.toByte
      pos += 1
    }

    /** Two ASCII chars. */
    inline def append2(c1: Char, c2: Char): Unit = {
      if (pos + 2 > ChunkSize) flush()
      val p = pos
      buf(p) = c1.toByte
      buf(p + 1) = c2.toByte
      pos = p + 2
    }

    def append(s: String): Unit = append(s, 0, s.length)

    /**
     * `s(from until to)`, encoded in UTF-8. A short one (names: tags, attributes) is copied char by char as it's
     * checked, in one pass (cheaper than a String.getBytes call for a few bytes); a longer one is checked, and then
     * copied in bulk if it's all ASCII.
     */
    def append(s: String, from: Int, to: Int): Unit = {
      val n = to - from
      if (n <= ShortString && n <= ChunkSize - pos) {
        var p = pos
        var i = from
        while (i < to) {
          val c = s.charAt(i)
          if (c >= 0x80) { // not ASCII: the rest is encoded
            pos = p
            appendUtf8(s, i, to)
            return
          }
          buf(p) = c.toByte
          p += 1
          i += 1
        }
        pos = p
      } else appendLong(s, from, to)
    }

    private def appendLong(s: String, from: Int, to: Int): Unit = {
      var bits = 0
      var i = from
      while (i < to) {
        bits |= s.charAt(i)
        i += 1
      }
      if (bits < 0x80) appendAscii(s, from, to) else appendUtf8(s, from, to)
    }

    /** `s(from until to)`, all ASCII, in as few copies as the buffer allows. */
    def appendAscii(s: String, from: Int, to: Int): Unit = {
      var start = from
      while (start < to) {
        if (pos == ChunkSize) flush()
        val chunk = math.min(to - start, ChunkSize - pos)
        copyAscii(s, start, start + chunk, buf, pos)
        pos += chunk
        start += chunk
      }
    }

    /** `s(from until to)`, encoded char by char (a few at a time, so a char's bytes are never split over two chunks). */
    def appendUtf8(s: String, from: Int, to: Int): Unit = {
      var start = from
      while (start < to) {
        if (ChunkSize - pos < 8) flush()
        // As many chars as surely fit (3 bytes each); a surrogate pair isn't split: it's 4 bytes for 2 chars.
        var end = math.min(to, start + (ChunkSize - pos) / 3)
        if (end < to && Character.isHighSurrogate(s.charAt(end - 1))) end -= 1
        if (end == start) end = math.min(to, start + 2)
        pos = encodeUtf8(s, start, end, buf, pos)
        start = end
      }
    }

    /** What's in `scratch` (then cleared): for the rare nodes only scala-xml knows how to write. */
    def appendScratch(): Unit = {
      val sb = _scratch.underlying
      if (sb.length > 0) {
        append(sb.toString)
        sb.setLength(0)
      }
    }

    inline def escape(text: String): Unit = XmlEscape.escape(text, this)

    def pushSingle(n: Node, scope: NamespaceBinding): Unit = {
      nodesStack(0) = n
      indexStack(0) = SingleNodeCursor
      scopeStack(0) = scope
      closingStack(0) = null
      depth = 1
    }

    def pushSeq(nodes: collection.Seq[Node], scope: NamespaceBinding, closing: Elem): Unit = {
      val d = depth
      if (d == nodesStack.length) growStacks()
      setLevel(d, nodes)
      scopeStack(d) = scope
      closingStack(d) = closing
      depth = d + 1
    }

    /**
     * Sets level `d` to read `nodes`, in the cheapest way for its kind. A Node is the NodeSeq of just itself (common:
     * e.g. an element's single child); any other NodeSeq is a wrapper, so its underlying seq is classified instead.
     */
    @tailrec private def setLevel(d: Int, nodes: collection.Seq[Node]): Unit = nodes match {
      case node: Node =>
        nodesStack(d) = node
        indexStack(d) = SingleNodeCursor
      case iseq: collection.IndexedSeq[Node] =>
        nodesStack(d) = iseq
        indexStack(d) = 0
        lengthStack(d) = iseq.length
      case ns: NodeSeq => setLevel(d, ns.theSeq)
      case cons: ::[Node] =>
        nodesStack(d) = cons
        indexStack(d) = ListCursor
      case Nil =>
        nodesStack(d) = IndexedSeq.empty
        indexStack(d) = 0
        lengthStack(d) = 0
      case other =>
        nodesStack(d) = other.iterator
        indexStack(d) = IteratorCursor
    }

    private def growStacks(): Unit = {
      val newCap = nodesStack.length * 2
      nodesStack = Arrays.copyOf(nodesStack, newCap)
      indexStack = Arrays.copyOf(indexStack, newCap)
      lengthStack = Arrays.copyOf(lengthStack, newCap)
      scopeStack = Arrays.copyOf(scopeStack, newCap)
      closingStack = Arrays.copyOf(closingStack, newCap)
    }
  }

  private inline def writeClosingTag(closing: Elem, out: Out): Unit = {
    out.append2('<', '/')
    appendName(closing.prefix, closing.label, out)
    out.append('>')
  }

  /** Serializes from where it stopped: returns true when done, false if it paused (or stopped after a failure). */
  private def writeAll(out: Out): Boolean = {
    while (out.depth > 0) {
      if (out.checkPause && out.shouldPause()) return false

      val d = out.depth - 1
      val idx = out.indexStack(d)
      var nextNode: Node = null

      if (idx >= 0) {
        if (idx < out.lengthStack(d)) {
          out.indexStack(d) = idx + 1
          nextNode = out.nodesStack(d).asInstanceOf[collection.IndexedSeq[Node]](idx)
        }
      } else if (idx == ListCursor) {
        val cons = out.nodesStack(d).asInstanceOf[::[Node]]
        nextNode = cons.head
        val tail = cons.tail
        if (tail.isEmpty) {
          out.nodesStack(d) = IndexedSeq.empty
          out.indexStack(d) = 0
          out.lengthStack(d) = 0
        } else {
          out.nodesStack(d) = tail
        }
      } else if (idx == IteratorCursor) {
        val it = out.nodesStack(d).asInstanceOf[Iterator[Node]]
        if (it.hasNext) nextNode = it.next()
      } else { // SingleNodeCursor
        nextNode = out.nodesStack(d).asInstanceOf[Node]
        out.nodesStack(d) = IndexedSeq.empty
        out.indexStack(d) = 0
        out.lengthStack(d) = 0
      }

      if (nextNode eq null) {
        val closing = out.closingStack(d)
        out.nodesStack(d) = null
        out.closingStack(d) = null
        out.depth = d
        if (closing ne null) writeClosingTag(closing, out)
      } else {
        val levelScope = out.scopeStack(d)
        nextNode match {
          case e: Elem =>
            out.append('<')
            appendName(e.prefix, e.label, out)
            e match {
              case fast: ElemOptimized =>
                var cur = fast.attrs
                while (cur.nonEmpty) {
                  val attr = cur.head
                  attr._2 match {
                    case value: String => appendAttribute(null, attr._1, value, out)
                    case js: Js => appendJsAttribute(attr._1, js, out)
                  }
                  cur = cur.tail
                }
              case _ =>
                val attrs = e.attributes
                if ((attrs ne null) && (attrs ne Null)) appendAttributes(attrs, out)
            }
            if ((e.scope ne levelScope) && (e.scope ne TopScope)) {
              e.scope.buildString(out.scratch, levelScope)
              out.appendScratch()
            }

            val children = e.child
            if (children.isEmpty) {
              if (e.minimizeEmpty) {
                out.append2('/', '>')
              } else {
                out.append('>')
                writeClosingTag(e, out)
              }
            } else {
              out.append('>')
              if (spacedAtoms(children)) {
                writeSpacedAtoms(children, e.scope, out)
                writeClosingTag(e, out)
              } else out.pushSeq(children, e.scope, e)
            }

          case t: Text if t.getClass eq classOf[Text] =>
            out.escape(t.data)

          case atom: Atom[?] if atom.getClass eq classOf[Atom[?]] =>
            out.escape(atom.data.toString)

          case u: Unparsed if u.getClass eq classOf[Unparsed] =>
            out.append(u.data)

          case g: Group =>
            // Its nodes are written in the Group's own scope (TopScope), as Utility.serialize does: so they redeclare
            // the namespaces they use, even ones the enclosing element declared.
            out.pushSeq(g.nodes, g.scope, null)

          case other =>
            writeRareNode(other, levelScope, out)
        }
      }
    }
    true
  }

  /**
   * As toString (Utility.serialize): when all of an element's children are Atoms but not Text (e.g. several `{value}`s
   * next to each other in a literal), they're written separated by spaces. Checked fully only when the first child is
   * one, and there are several.
   */
  private def spacedAtoms(children: collection.Seq[Node]): Boolean =
    isAtomNotText(children.head) && children.lengthCompare(1) > 0 && children.forall(isAtomNotText)

  private def isAtomNotText(n: Node): Boolean = n.isInstanceOf[Atom[?]] && !n.isInstanceOf[Text]

  /** Atoms (see [[spacedAtoms]]), separated by spaces. */
  @noinline private def writeSpacedAtoms(children: collection.Seq[Node], scope: NamespaceBinding, out: Out): Unit = {
    var first = true
    children.foreach { n =>
      if (!first) out.append(' ')
      first = false
      n match {
        case atom: Atom[?] if atom.getClass eq classOf[Atom[?]] => out.escape(atom.data.toString)
        case u: Unparsed if u.getClass eq classOf[Unparsed] => out.append(u.data)
        case other => writeRareNode(other, scope, out)
      }
    }
  }

  /** Kept out-of-line so `writeAll` stays small and register-friendly for HotSpot C2. */
  @noinline private def writeRareNode(node: Node, levelScope: NamespaceBinding, out: Out): Unit = node match {
    case special: SpecialNode =>
      special.buildString(out.scratch)
      out.appendScratch()
    case other =>
      Utility.serialize(other, levelScope, out.scratch)
      out.appendScratch()
  }

  private inline def appendName(prefix: String, label: String, out: Out): Unit = {
    if (prefix ne null) {
      out.append(prefix)
      out.append(':')
    }
    out.append(label)
  }

  /** ` key="..."`, with the parts of `js` (see [[Js.foreachPart]]) escaped straight into the buffer: never joined. */
  private def appendJsAttribute(key: String, js: Js, out: Out): Unit = {
    out.append(' ')
    out.append(key)
    out.append2('=', '"')
    js.foreachPart(part => XmlEscape.escape(part, out))
    out.append('"')
  }

  private def appendAttribute(pre: String, key: String, value: String, out: Out): Unit = {
    out.append(' ')
    appendName(pre, key, out)
    out.append2('=', '"')
    out.escape(value)
    out.append('"')
  }

  /**
   * The attributes of a plain Elem, as MetaData.buildString writes them. Values that are a single plain [[Text]] or
   * [[Atom]] (nearly all) are escaped straight into the buffer; any other value (Unparsed, PCData, entity refs, several
   * nodes) or other MetaData type goes through scala-xml for that one attribute, which writes those its own way (e.g.
   * a value with a `"` in single quotes, PCData as CDATA).
   */
  @tailrec private def appendAttributes(attributes: MetaData, out: Out): Unit = attributes match {
    case Null =>
    case a: Attribute if a.value eq null => // not a real attribute (e.g. the placeholder heading an ElemOptimized's)
      appendAttributes(a.next, out)
    case a: Attribute if isSinglePlainText(a.value) =>
      appendAttribute(if (a.isPrefixed) a.pre else null, a.key, a.value.head.asInstanceOf[Atom[?]].data.toString, out)
      appendAttributes(a.next, out)
    case other =>
      other.copy(Null).buildString(out.scratch) // just this attribute, including its leading ' '
      out.appendScratch()
      appendAttributes(other.next, out)
  }

  private def isSinglePlainText(value: collection.Seq[Node]): Boolean =
    value.lengthCompare(1) == 0 && {
      val cls = value.head.getClass
      (cls eq classOf[Text]) || (cls eq classOf[Atom[?]])
    }
}
