package com.fastscala.scala_xml.xml

import com.fastscala.js.{CompositeJs, EncJs, Js, JsWriter}
import com.fastscala.scala_xml.js.{JS, JsWithScriptDetection, NodeSeqJs}
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.{Blocker, Callback}

import java.nio.ByteBuffer
import java.util.Arrays
import scala.annotation.tailrec
import scala.collection.immutable.{ArraySeq, IndexedSeq}
import scala.collection.mutable.ArrayBuffer
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
 * iteratively (not recursively): a [[CompositeJs]]'s children are a level, and so is a
 * [[NodeSeqJs]]'s XML (written as XML, raw: it's its code), and an [[EncJs]]'s Js: a level that's escaped (see
 * [[Encoder]]), as are those in it (escaped once more for each EncJs they're in); any other Js is written as its `cmd`, as it is (not escaped).
 *
 * A [[JsWithScriptDetection]]'s XML is a level too, with a collector (see `Out.collector`): its JS `<script>` tags
 * aren't written; their JavaScript is collected as they're reached, and written after the XML (what its `scripts` makes
 * of it). So the XML is neither searched nor copied beforehand.
 */
object XmlJsStream extends JsWriter {

  val ChunkSize = 8192

  // The special values of Level.index (a value >= 0 is the cursor into an indexed seq):
  /** The level's nodes are a non-empty `::` (List), read head by head: `nodes` holds the remaining list. */
  private final val ListCursor = -1
  /** The level's nodes are read with an Iterator (any other kind of seq): `nodes` holds the iterator. */
  private final val IteratorCursor = -2
  /** The level is a single node (writeNode's root, or a Node used as its own child seq): `nodes` holds the node. */
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

  def writeSeq(ns: NodeSeq, sink: Content.Sink, prefix: String, callback: Callback): Unit =
    writeSeq(ns, sink, prefix, callback, endContent = true)

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

  def writeJs(js: Js, sink: Content.Sink, callback: Callback): Unit = writeJs(js, sink, callback, endContent = true)

  override def writeJs(js: Js, sink: Content.Sink, callback: Callback, endContent: Boolean): Unit = {
    val out = new Out(sink, callback, endContent)
    out.pushSingle(js, TopScope)
    out.drive()
  }

  def writeJs(js: Js, sink: Content.Sink): Unit = blocking(writeJs(js, sink, _))

  def writeNode(n: Node, sink: Content.Sink, callback: Callback): Unit = {
    val out = new Out(sink, callback, endContent = true)
    out.pushSingle(n, TopScope)
    out.drive()
  }

  def writeSeq(ns: NodeSeq, sink: Content.Sink, prefix: String = ""): Unit =
    blocking(writeSeq(ns, sink, prefix, _))

  def writeNode(n: Node, sink: Content.Sink): Unit = blocking(writeNode(n, sink, _))

  private def blocking(write: Callback => Unit): Unit = {
    val callback = Blocker.callback()
    try {
      write(callback)
      callback.block()
    } finally callback.close()
  }

  /**
   * A level of the stack (see [[Out.levels]]): the items being read, where the reading is at, and what applies to them.
   * Mutable, and reused: a stack slot's Level is created once, and set again by each push that lands on it.
   */
  private[xml] final class Level {
    var nodes: AnyRef = null
    var index = 0
    var length = 0
    var scope: NamespaceBinding = null
    var closing: AnyRef = null
    var markupEsc: Encoder.Escaping = Encoder.Unescaped
    var textEsc: Encoder.Escaping = Encoder.Xml1
    var collector: ArrayBuffer[Js] = null

    def setEmpty(): Unit = {
      nodes = IndexedSeq.empty
      index = 0
      length = 0
    }
  }

  private final class Chunk(val bytes: Array[Byte], val length: Int, val last: Boolean, val endsContent: Boolean)

  private[xml] final class Out(sink: Content.Sink, callback: Callback, endContent: Boolean) extends EncoderOutput {
    var buf = new Array[Byte](ChunkSize)
    var pos = 0

    private val lock = new Object
    private val queued = new java.util.ArrayDeque[Chunk]()
    private val freeBuffers = new java.util.ArrayDeque[Array[Byte]]()
    private var writing = false
    private var paused = false
    private var failure: Throwable = null
    private var completed = false

    var checkPause = false
    private var _scratch: StringBuilder = null

    def scratch: StringBuilder = {
      if (_scratch eq null) _scratch = new StringBuilder(256)
      _scratch
    }

    var depth = 0
    var levels = new Array[Level](64)

    var markupEsc: Encoder.Escaping = Encoder.Unescaped
    var textEsc: Encoder.Escaping = Encoder.Xml1
    var collector: ArrayBuffer[Js] = null

    inline def setEscaping(markup: Encoder.Escaping, text: Encoder.Escaping): Unit = {
      markupEsc = markup
      textEsc = text
    }

    def drive(): Unit =
      try {
        if (writeAll(this)) finish()
      } catch {
        case t: Throwable => fail(t)
      }

    def flush(): Unit = if (pos > 0) queue(last = false)
    def finish(): Unit = queue(last = true)

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

    def shouldPause(): Boolean = lock.synchronized {
      checkPause = false
      if (failure ne null) true
      else if (queued.size >= MaxQueuedChunks) {
        paused = true
        true
      } else false
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

    def fail(t: Throwable): Unit = {
      lock.synchronized {
        if (failure eq null) failure = t
        checkPause = true
      }
      complete(t)
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

    inline def append(c: Char): Unit = Encoder.writeAscii(c, this, markupEsc)
    inline def append2(c1: Char, c2: Char): Unit = Encoder.writeAscii2(c1, c2, this, markupEsc)
    inline def append(s: String): Unit = Encoder.write(s, this, markupEsc)

    def appendScratch(): Unit = {
      val sb = _scratch.underlying
      if (sb.length > 0) {
        Encoder.write(sb, this, markupEsc)
        sb.setLength(0)
      }
    }

    inline def escape(text: String): Unit = Encoder.write(text, this, textEsc)

    def pushSingle(n: AnyRef, scope: NamespaceBinding): Unit = {
      depth = 0
      val level = push()
      level.nodes = n
      level.index = SingleNodeCursor
      level.scope = scope
      level.closing = null
      level.markupEsc = Encoder.Unescaped
      level.textEsc = Encoder.Xml1
      level.collector = null
      setEscaping(Encoder.Unescaped, Encoder.Xml1)
      collector = null
    }

    private def push(): Level = {
      val d = depth
      if (d == levels.length) levels = Arrays.copyOf(levels, d * 2)
      var level = levels(d)
      if (level eq null) {
        level = new Level
        levels(d) = level
      }
      depth = d + 1
      level
    }

    def pushSeq(nodes: collection.Seq[AnyRef], scope: NamespaceBinding, closing: AnyRef): Unit = {
      val level = push()
      setNodes(level, nodes)
      level.scope = scope
      level.closing = closing
      level.markupEsc = markupEsc
      level.textEsc = textEsc
      level.collector = collector
    }

    def pushCollecting(ns: NodeSeq, scripts: ArrayBuffer[Js]): Unit = {
      val nextMarkup = markupEsc.enterJs
      val nextText = nextMarkup.nextXml
      pushSeq(ns, TopScope, null)
      val level = levels(depth - 1)
      level.markupEsc = nextMarkup
      level.textEsc = nextText
      level.collector = scripts
      setEscaping(nextMarkup, nextText)
      collector = scripts
    }

    def pushEscaped(js: Js, scope: NamespaceBinding, closing: AnyRef = null): Unit = {
      val nextMarkup = markupEsc.enterJs
      pushSingleJs(js, scope, closing, nextMarkup, nextMarkup.nextXml)
    }

    def pushAttrValue(js: Js, scope: NamespaceBinding): Unit = {
      val nextMarkup = textEsc
      pushSingleJs(js, scope, AttrValueEnd, nextMarkup, nextMarkup.nextXml)
    }

    private def pushSingleJs(js: Js, scope: NamespaceBinding, closing: AnyRef, nextMarkup: Encoder.Escaping, nextText: Encoder.Escaping): Unit = {
      val level = push()
      level.nodes = js
      level.index = SingleNodeCursor
      level.scope = scope
      level.closing = closing
      level.markupEsc = nextMarkup
      level.textEsc = nextText
      level.collector = null
      collector = null
      setEscaping(nextMarkup, nextText)
    }

    @tailrec private def setNodes(level: Level, nodes: collection.Seq[AnyRef]): Unit = nodes match {
      case node: Node =>
        level.nodes = node
        level.index = SingleNodeCursor
      case iseq: collection.IndexedSeq[?] =>
        level.nodes = iseq
        level.index = 0
        level.length = iseq.length
      case ns: NodeSeq => setNodes(level, ns.theSeq)
      case cons: ::[AnyRef] =>
        level.nodes = cons
        level.index = ListCursor
      case Nil =>
        level.setEmpty()
      case other =>
        level.nodes = other.iterator
        level.index = IteratorCursor
    }
  }

  private inline def writeClosingTag(closing: Elem, out: Out): Unit = {
    out.append2('<', '/')
    appendName(closing.prefix, closing.label, out)
    out.append('>')
  }

  /** Main dispatch loop split via method inlining to keep code size small for HotSpot C2. */
  private def writeAll(out: Out): Boolean = {
    while (out.depth > 0) {
      if (out.checkPause && out.shouldPause()) return false

      val d = out.depth - 1
      val level = out.levels(d)
      val idx = level.index
      var nextNode: AnyRef = null

      if (idx >= 0) {
        if (idx < level.length) {
          level.index = idx + 1
          nextNode = level.nodes.asInstanceOf[collection.IndexedSeq[AnyRef]](idx)
        }
      } else if (idx == ListCursor) {
        val cons = level.nodes.asInstanceOf[::[AnyRef]]
        nextNode = cons.head
        val tail = cons.tail
        if (tail.isEmpty) level.setEmpty()
        else level.nodes = tail
      } else if (idx == IteratorCursor) {
        val it = level.nodes.asInstanceOf[Iterator[AnyRef]]
        if (it.hasNext) nextNode = it.next()
      } else {
        nextNode = level.nodes
        level.setEmpty()
      }

      if (nextNode eq null) {
        popLevel(level, out, d)
      } else {
        writeNodeInline(nextNode, level.scope, out)
      }
    }
    true
  }

  @inline private def popLevel(level: Level, out: Out, d: Int): Unit = {
    val closing = level.closing
    level.nodes = null
    level.scope = null
    level.closing = null
    level.collector = null
    out.depth = d
    if (d > 0) {
      val parent = out.levels(d - 1)
      out.setEscaping(parent.markupEsc, parent.textEsc)
      out.collector = parent.collector
    } else {
      out.setEscaping(Encoder.Unescaped, Encoder.Xml1)
      out.collector = null
    }
    closing match {
      case null =>
      case e: Elem => writeClosingTag(e, out)
      case _ => out.append('"')
    }
  }

  @inline private def writeNodeInline(nextNode: AnyRef, levelScope: NamespaceBinding, out: Out): Unit = {
    nextNode match {
      case e: Elem if (out.collector ne null) && JS.collectJsOfScriptTag(e, out.collector) =>
      case e: Elem => writeElemInline(e, levelScope, out)
      case t: Text if t.getClass eq classOf[Text] => out.escape(t.data)
      case atom: Atom[?] if atom.getClass eq classOf[Atom[?]] => out.escape(atom.data.toString)
      case u: Unparsed if u.getClass eq classOf[Unparsed] => out.append(u.data)
      case g: Group => out.pushSeq(g.nodes, g.scope, null)
      case js: Js => writeJsItem(js, levelScope, out)
      case escaped: Escaped => out.pushEscaped(escaped.js, levelScope)
      case xml: XmlWithoutScripts => out.pushCollecting(xml.ns, xml.scripts)
      case collected: CollectedScripts => writeCollectedScripts(collected, levelScope, out)
      case attr: JsAttr =>
        if (needsLevel(attr.js)) {
          appendAttrStart(attr.key, out)
          out.pushAttrValue(attr.js, levelScope)
        } else appendJsAttribute(attr.key, attr.js, out)
      case attr: StringAttr => appendAttribute(null, attr.key, attr.value, out)
      case end: StartTagEnd => endStartTag(end.elem, levelScope, out)
      case other: Node => writeRareNode(other, levelScope, out)
    }
  }

  @inline private def writeElemInline(e: Elem, levelScope: NamespaceBinding, out: Out): Unit = {
    out.append('<')
    appendName(e.prefix, e.label, out)
    e match {
      case fast: ElemOptimized =>
        var cur = fast.attrs
        var pushed = false
        while (cur.nonEmpty && !pushed) {
          val attr = cur.head
          attr._2 match {
            case value: String => appendAttribute(null, attr._1, value, out)
            case js: Js =>
              if (needsLevel(js)) {
                out.pushSeq(startTagRest(cur, fast), levelScope, null)
                pushed = true
              } else appendJsAttribute(attr._1, js, out)
          }
          cur = cur.tail
        }
        if (!pushed) endStartTag(e, levelScope, out)
      case _ =>
        val attrs = e.attributes
        if ((attrs ne null) && (attrs ne Null)) appendAttributes(attrs, out)
        endStartTag(e, levelScope, out)
    }
  }

  private def writeJsItem(js: Js, scope: NamespaceBinding, out: Out): Unit = js match {
    case composite: CompositeJs =>
      out.pushSeq(composite.children, scope, null)
    case nodes: NodeSeqJs =>
      out.pushSeq(nodes.ns, TopScope, null)
    case enc: EncJs =>
      if (enc.inString) {
        out.append('"')
        out.pushEscaped(enc.js, scope, AttrValueEnd)
      } else {
        out.pushEscaped(enc.js, scope, null)
      }
    case detection: JsWithScriptDetection =>
      val scripts = new ArrayBuffer[Js]()
      val items = Array[AnyRef](
        detection.before,
        Quote,
        new XmlWithoutScripts(detection.ns, scripts),
        Quote,
        detection.after,
        new CollectedScripts(detection, scripts)
      )
      out.pushSeq(ArraySeq.unsafeWrapArray(items), scope, null)
    case other =>
      out.append(codeOf(other))
  }

  private def codeOf(js: Js): String = {
    val cmd = js.cmd
    if (cmd eq null) "null" else cmd
  }

  private val Quote: Js = Js("\"")
  private val Semicolon: Js = Js(";")

  private final class XmlWithoutScripts(val ns: NodeSeq, val scripts: ArrayBuffer[Js])
  private final class CollectedScripts(val detection: JsWithScriptDetection, val scripts: ArrayBuffer[Js])
  private final class Escaped(val js: Js)

  private def writeCollectedScripts(collected: CollectedScripts, scope: NamespaceBinding, out: Out): Unit = {
    val extracted = if (collected.scripts.isEmpty) Js.Void else CompositeJs.joined(collected.scripts, Semicolon)
    val js = collected.detection.scripts(extracted)
    if (js != Js.Void) {
      out.append(';')
      writeJsItem(js, scope, out)
    }
  }

  private final class JsAttr(val key: String, val js: Js)
  private final class StringAttr(val key: String, val value: String)
  private final class StartTagEnd(val elem: Elem)
  private object AttrValueEnd

  private def needsLevel(js: Js): Boolean = js match {
    case composite: CompositeJs =>
      composite.children match {
        case iseq: collection.IndexedSeq[Js] =>
          val len = iseq.length
          var i = 0
          while (i < len) {
            if (needsLevel(iseq(i))) return true
            i += 1
          }
          false
        case other => other.exists(needsLevel)
      }
    case _: EncJs | _: NodeSeqJs | _: JsWithScriptDetection => true
    case _ => false
  }

  private def startTagRest(from: List[(String, ElemOptimized.AttrValue)], elem: ElemOptimized): IndexedSeq[AnyRef] = {
    val buf = new Array[AnyRef](from.length + 1)
    var cur = from
    var i = 0
    while (cur.nonEmpty) {
      val attr = cur.head
      buf(i) = attr._2 match {
        case value: String => new StringAttr(attr._1, value)
        case js: Js        => new JsAttr(attr._1, js)
      }
      i += 1
      cur = cur.tail
    }
    buf(i) = new StartTagEnd(elem)
    ArraySeq.unsafeWrapArray(buf)
  }

  private def endStartTag(e: Elem, levelScope: NamespaceBinding, out: Out): Unit = {
    if ((e.scope ne levelScope) && (e.scope ne TopScope)) {
      appendScope(e.scope, levelScope, out)
    }

    val children = e.child
    children match {
      case t: Text if t.getClass eq classOf[Text] =>
        out.append('>')
        out.escape(t.data)
        writeClosingTag(e, out)
      case iseq: collection.IndexedSeq[Node] if iseq.length == 1 && (iseq(0).getClass eq classOf[Text]) =>
        out.append('>')
        out.escape(iseq(0).asInstanceOf[Text].data)
        writeClosingTag(e, out)
      case _ if children.isEmpty =>
        if (e.minimizeEmpty) {
          out.append2('/', '>')
        } else {
          out.append('>')
          writeClosingTag(e, out)
        }
      case _ =>
        out.append('>')
        if (spacedAtoms(children)) {
          writeSpacedAtoms(children, e.scope, out)
          writeClosingTag(e, out)
        } else out.pushSeq(children, e.scope, e)
    }
  }

  @tailrec private def appendScope(scope: NamespaceBinding, stop: NamespaceBinding, out: Out): Unit = {
    if ((scope ne null) && (scope ne TopScope) && (scope ne stop)) {
      out.append(" xmlns")
      val prefix = scope.prefix
      if (prefix ne null) {
        out.append(':')
        out.append(prefix)
      }
      out.append2('=', '"')
      val uri = scope.uri
      if (uri ne null) out.escape(uri)
      out.append('"')
      appendScope(scope.parent, stop, out)
    }
  }

  private def spacedAtoms(children: collection.Seq[Node]): Boolean = children match {
    case iseq: collection.IndexedSeq[Node] =>
      val len = iseq.length
      if (len <= 1 || !isAtomNotText(iseq(0))) false
      else {
        var i = 1
        while (i < len) {
          if (!isAtomNotText(iseq(i))) return false
          i += 1
        }
        true
      }
    case _ =>
      isAtomNotText(children.head) && children.lengthCompare(1) > 0 && children.forall(isAtomNotText)
  }

  private inline def isAtomNotText(n: Node): Boolean = n.isInstanceOf[Atom[?]] && !n.isInstanceOf[Text]

  @noinline private def writeSpacedAtoms(children: collection.Seq[Node], scope: NamespaceBinding, out: Out): Unit =
    children match {
      case iseq: collection.IndexedSeq[Node] =>
        val len = iseq.length
        var i = 0
        while (i < len) {
          if (i > 0) out.append(' ')
          writeAtomOrRare(iseq(i), scope, out)
          i += 1
        }
      case _ =>
        var first = true
        children.foreach { n =>
          if (!first) out.append(' ')
          first = false
          writeAtomOrRare(n, scope, out)
        }
    }

  private inline def writeAtomOrRare(n: Node, scope: NamespaceBinding, out: Out): Unit = n match {
    case atom: Atom[?] if atom.getClass eq classOf[Atom[?]] => out.escape(atom.data.toString)
    case u: Unparsed if u.getClass eq classOf[Unparsed] => out.append(u.data)
    case other => writeRareNode(other, scope, out)
  }

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

  private def appendAttrStart(key: String, out: Out): Unit = {
    out.append(' ')
    out.append(key)
    out.append2('=', '"')
  }

  private def appendJsAttribute(key: String, js: Js, out: Out): Unit = {
    appendAttrStart(key, out)
    escapeJs(js, out)
    out.append('"')
  }

  private def escapeJs(js: Js, out: Out): Unit = js match {
    case composite: CompositeJs =>
      composite.children match {
        case iseq: collection.IndexedSeq[Js] =>
          val len = iseq.length
          var i = 0
          while (i < len) {
            escapeJs(iseq(i), out)
            i += 1
          }
        case other => other.foreach(escapeJs(_, out))
      }
    case other => out.escape(codeOf(other))
  }

  private def appendAttribute(pre: String, key: String, value: String, out: Out): Unit = {
    out.append(' ')
    appendName(pre, key, out)
    out.append2('=', '"')
    out.escape(value)
    out.append('"')
  }

  @tailrec private def appendAttributes(attributes: MetaData, out: Out): Unit = attributes match {
    case Null =>
    case a: Attribute if a.value eq null =>
      appendAttributes(a.next, out)
    case a: Attribute if isSinglePlainText(a.value) =>
      val textVal = a.value match {
        case t: Text => t.data
        case n: Node => n.asInstanceOf[Atom[?]].data.toString
        case seq =>
          seq.head match {
            case t: Text => t.data
            case head => head.asInstanceOf[Atom[?]].data.toString
          }
      }
      appendAttribute(if (a.isPrefixed) a.pre else null, a.key, textVal, out)
      appendAttributes(a.next, out)
    case other =>
      other.copy(Null).buildString(out.scratch)
      out.appendScratch()
      appendAttributes(other.next, out)
  }

  private def isSinglePlainText(value: collection.Seq[Node]): Boolean = value match {
    case n: Node =>
      val cls = n.getClass
      (cls eq classOf[Text]) || (cls eq classOf[Atom[?]])
    case _ =>
      value.lengthCompare(1) == 0 && {
        val cls = value.head.getClass
        (cls eq classOf[Text]) || (cls eq classOf[Atom[?]])
      }
  }
}
