package com.fastscala.scala_xml.xml

import com.fastscala.js.{CompositeJs, EncJs, Js, JsWriter}
import com.fastscala.scala_xml.js.NodeSeqJs
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.{Blocker, Callback}

import java.nio.ByteBuffer
import java.util.Arrays
import scala.annotation.tailrec
import scala.collection.immutable.IndexedSeq
import scala.xml.*

/**
 * Writes XML and JavaScript ([[Js]]): XML as `Node.toString` would (attributes of an [[ElemOptimized]] in its own order), encoded in UTF-8,
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
 *
 * JavaScript ([[writeJs]]) goes through the same stack as the XML (a level's items can be Nodes or Js), so it's written
 * iteratively, not recursively (as Js.writeUtf8Enc does): a [[CompositeJs]]'s children are a level, and so is a
 * [[NodeSeqJs]]'s XML (written as XML, raw: it's its code), and an [[EncJs]]'s Js: a level that's escaped (see
 * [[Encoder]]), as are those in it (escaped once more for each EncJs they're in); any other Js is written as its `cmd`, as it is (not escaped).
 */
object XmlJsStream extends JsWriter {

  val ChunkSize = 8192

  // The special values of Out.indexStack (a value >= 0 is the cursor into an indexed seq):
  /** The level's nodes are a non-empty `::` (List), read head by head: nodesStack holds the remaining list. */
  private final val ListCursor = -1
  /** The level's nodes are read with an Iterator (any other kind of seq): nodesStack holds the iterator. */
  private final val IteratorCursor = -2
  /** The level is a single node (writeNode's root, or a Node used as its own child seq): nodesStack holds the node. */
  private final val SingleNodeCursor = -3

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
  def writeSeq(ns: NodeSeq, sink: Content.Sink, prefix: String, callback: Callback): Unit =
    writeSeq(ns, sink, prefix, callback, endContent = true)

  /**
   * As the other [[writeSeq]], but the last write ends the sink's content only if `endContent`: with false, the XML is
   * a part of the content (e.g. JavaScript with the XML in it, written to a response), so the last write is an ordinary
   * one (and `callback` is still completed after it).
   */
  def writeSeq(ns: NodeSeq, sink: Content.Sink, prefix: String, callback: Callback, endContent: Boolean): Unit = {
    val out = new Out(sink, callback, endContent)
    try {
      out.append(prefix)
      out.pushSeq(ns, TopScope, null)
    } catch {
      case t: Throwable => out.fail(t)
    }
    out.drive()
  }

  /**
   * Writes `js` (its code, as it is: not escaped), and ends the sink's content, without blocking: as [[writeSeq]] (the
   * callback completed once, after the last write, or failed on the first failure).
   */
  def writeJs(js: Js, sink: Content.Sink, callback: Callback): Unit = writeJs(js, sink, callback, endContent = true)

  /** As the other [[writeJs]], but the last write ends the sink's content only if `endContent` (see [[writeSeq]]). */
  override def writeJs(js: Js, sink: Content.Sink, callback: Callback, endContent: Boolean): Unit = {
    val out = new Out(sink, callback, endContent)
    out.pushSingle(js, TopScope)
    out.drive()
  }

  /** As the non-blocking [[writeJs]], but returns once everything is written, and throws if writing fails. */
  def writeJs(js: Js, sink: Content.Sink): Unit = blocking(writeJs(js, sink, _))

  /** As [[writeSeq]], for one node. */
  def writeNode(n: Node, sink: Content.Sink, callback: Callback): Unit = {
    val out = new Out(sink, callback, endContent = true)
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

  /** A filled chunk, waiting to be written (or being written). */
  private final class Chunk(val bytes: Array[Byte], val length: Int, val last: Boolean, val endsContent: Boolean)

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
  private[xml] final class Out(sink: Content.Sink, callback: Callback, endContent: Boolean) {
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
    /** Each level's JavaScript escaping: how many EncJs's it's in (0: none). */
    var jsDepthStack = new Array[Int](64)

    /**
     * The current level's JavaScript escaping: how many EncJs's it's in (0: none). Everything written (the markup, and
     * text and attribute values) is escaped for JavaScript as many times, by [[Encoder]].
     */
    var jsDepth = 0

    /** Changes the JavaScript escaping (how many EncJs's what's written next is in). */
    def setJsDepth(d: Int): Unit = jsDepth = d

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

    /** (`last`: the last chunk, after which the callback is completed; it ends the sink's content if `endContent`.) */
    private def queue(last: Boolean): Unit = {
      val chunk = new Chunk(buf, pos, last, endsContent = last && endContent)
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
        sink.write(chunk.endsContent, ByteBuffer.wrap(chunk.bytes, 0, chunk.length), new Callback {
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

    // ---------- writing: all through Encoder (escaping, as the current level needs, and UTF-8) ----------

    /** An ASCII char of the markup's own (`<`, `>`, `/`, `=`, `"`, ` `, `:`). */
    inline def append(c: Char): Unit = Encoder.writeAscii(c, this, jsDepth)

    /** Two ASCII chars of the markup's own. */
    inline def append2(c1: Char, c2: Char): Unit = {
      Encoder.writeAscii(c1, this, jsDepth)
      Encoder.writeAscii(c2, this, jsDepth)
    }

    /** `s`, as it is (markup, or code): not escaped for XML. */
    def append(s: String): Unit = Encoder.write(s, this, xml = false, jsDepth)

    /** What's in `scratch` (then cleared): for the rare nodes only scala-xml knows how to write. */
    def appendScratch(): Unit = {
      val sb = _scratch.underlying
      if (sb.length > 0) {
        Encoder.write(sb, this, xml = false, jsDepth)
        sb.setLength(0)
      }
    }

    /** `text`, escaped for XML (text, or an attribute's value). */
    def escape(text: String): Unit = Encoder.write(text, this, xml = true, jsDepth)

    /** The first level: a single item (a Node or a Js). */
    def pushSingle(n: AnyRef, scope: NamespaceBinding): Unit = {
      nodesStack(0) = n
      indexStack(0) = SingleNodeCursor
      scopeStack(0) = scope
      closingStack(0) = null
      jsDepthStack(0) = 0
      depth = 1
    }

    /** A level of items: Nodes, or Js (a CompositeJs's children). */
    def pushSeq(nodes: collection.Seq[AnyRef], scope: NamespaceBinding, closing: Elem): Unit = {
      val d = depth
      if (d == nodesStack.length) growStacks()
      setLevel(d, nodes)
      scopeStack(d) = scope
      closingStack(d) = closing
      jsDepthStack(d) = jsDepth // (as the level it's in)
      depth = d + 1
    }

    /** A level of just `js`, escaped (once more than the current level: for an EncJs). */
    def pushEscaped(js: Js, scope: NamespaceBinding): Unit = {
      val d = depth
      if (d == nodesStack.length) growStacks()
      nodesStack(d) = js
      indexStack(d) = SingleNodeCursor
      scopeStack(d) = scope
      closingStack(d) = null
      jsDepthStack(d) = jsDepth + 1
      depth = d + 1
      setJsDepth(jsDepth + 1)
    }

    /**
     * Sets level `d` to read `nodes`, in the cheapest way for its kind. A Node is the NodeSeq of just itself (common:
     * e.g. an element's single child); any other NodeSeq is a wrapper, so its underlying seq is classified instead.
     */
    @tailrec private def setLevel(d: Int, nodes: collection.Seq[AnyRef]): Unit = nodes match {
      case node: Node =>
        nodesStack(d) = node
        indexStack(d) = SingleNodeCursor
      case iseq: collection.IndexedSeq[?] =>
        nodesStack(d) = iseq
        indexStack(d) = 0
        lengthStack(d) = iseq.length
      case ns: NodeSeq => setLevel(d, ns.theSeq)
      case cons: ::[AnyRef] =>
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
      jsDepthStack = Arrays.copyOf(jsDepthStack, newCap)
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
      var nextNode: AnyRef = null // a Node or a Js

      if (idx >= 0) {
        if (idx < out.lengthStack(d)) {
          out.indexStack(d) = idx + 1
          nextNode = out.nodesStack(d).asInstanceOf[collection.IndexedSeq[AnyRef]](idx)
        }
      } else if (idx == ListCursor) {
        val cons = out.nodesStack(d).asInstanceOf[::[AnyRef]]
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
        val it = out.nodesStack(d).asInstanceOf[Iterator[AnyRef]]
        if (it.hasNext) nextNode = it.next()
      } else { // SingleNodeCursor
        nextNode = out.nodesStack(d)
        out.nodesStack(d) = IndexedSeq.empty
        out.indexStack(d) = 0
        out.lengthStack(d) = 0
      }

      if (nextNode eq null) {
        val closing = out.closingStack(d)
        out.nodesStack(d) = null
        out.closingStack(d) = null
        out.depth = d
        // (Back to the escaping of the level it's in: before the closing tag, which is that level's.)
        out.setJsDepth(if (d > 0) out.jsDepthStack(d - 1) else 0)
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

          case js: Js =>
            writeJsItem(js, levelScope, out)

          case escaped: Escaped =>
            out.pushEscaped(escaped.js, levelScope)

          case other: Node =>
            writeRareNode(other, levelScope, out)
        }
      }
    }
    true
  }

  /**
   * A Js:
   *  - a [[CompositeJs]]'s children are pushed, as a level;
   *  - a [[NodeSeqJs]]'s nodes are pushed, as a level of XML: its code is the XML as it is (as its writeUtf8Enc writes
   *    it), in its own scope (TopScope, as its `cmd`, toString, does), whatever XML the Js is in;
   *  - an [[EncJs]]'s Js is pushed, as a level that's escaped once more than this one (its quotes, if `inString`,
   *    are written at this level: so they're escaped by the EncJs's this one is in, if any, as its `cmd` does);
   *  - any other is written as its `cmd` (for now: specific kinds can be written without building it).
   */
  private def writeJsItem(js: Js, scope: NamespaceBinding, out: Out): Unit = js match {
    case composite: CompositeJs =>
      out.pushSeq(composite.children, scope, null)
    case nodes: NodeSeqJs =>
      out.pushSeq(nodes.ns, TopScope, null)
    case enc: EncJs =>
      if (enc.inString) out.pushSeq(Vector(Quote, new Escaped(enc.js), Quote), scope, null)
      else out.pushEscaped(enc.js, scope)
    case other =>
      out.append(other.cmd)
  }

  private val Quote: Js = Js("\"")

  /** An item of a level: `js`, to be pushed as a level of its own, escaped once more (an EncJs's, between quotes). */
  private final class Escaped(val js: Js)

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

  /** ` key="..."`, with `js` escaped straight into the buffer: a CompositeJs child by child, never joined. */
  private def appendJsAttribute(key: String, js: Js, out: Out): Unit = {
    out.append(' ')
    out.append(key)
    out.append2('=', '"')
    escapeJs(js, out)
    out.append('"')
  }

  /** `js`'s code, escaped: a CompositeJs's children one at a time (its `cmd` isn't built); any other Js, its `cmd`. */
  private def escapeJs(js: Js, out: Out): Unit = js match {
    case composite: CompositeJs => composite.children.foreach(escapeJs(_, out))
    case other => out.escape(other.cmd)
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
