package com.fastscala.scala_xml.xml.streaming

import com.fastscala.js.{CompositeJs, EncJs, Js, JsWriter}
import com.fastscala.scala_xml.js.{JS, JsWithScriptDetection, NodeSeqJs}
import com.fastscala.scala_xml.xml.ElemOptimized
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.{Blocker, Callback}

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
 * Writing doesn't block (with a callback: see [[writeSeq]]), as [[NonBlockingChunkedSinkWriter]] does it: one write to the sink at
 * a time (as Jetty requires), while serializing goes on into the next chunk; once [[MaxQueuedChunks]] chunks are waiting
 * for their turn, serializing pauses (between nodes), and the write that drains the queue resumes it, on its thread. So
 * a slow client doesn't hold a thread, and only a few chunks are kept (more only for a single node bigger than that,
 * e.g. a huge text).
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

  /** The size of each write but the last (see [[NonBlockingChunkedSinkWriter]]). */
  val ChunkSize: Int = NonBlockingChunkedSinkWriter.ChunkSize

  /** Serializing pauses (between nodes) once this many chunks are waiting to be written (see [[NonBlockingChunkedSinkWriter]]). */
  val MaxQueuedChunks: Int = NonBlockingChunkedSinkWriter.MaxQueued

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

  /** Main dispatch loop split via method inlining to keep code size small for HotSpot C2. */
  def writeAll(out: Out): Boolean = {
    while (out.depth > 0) {
      if (out.checkPause && out.shouldPause()) return false

      val d = out.depth - 1
      val level = out.levels(d)
      val idx = level.index
      var nextNode: AnyRef = null

      if (idx >= 0) {
        if (idx < level.length) {
          level.index = idx + 1
          nextNode = level.items(idx)
        }
      } else if (idx == Out.ListCursor) {
        val cons = level.nodes.asInstanceOf[::[AnyRef]]
        nextNode = cons.head
        val tail = cons.tail
        if (tail.isEmpty) level.setEmpty()
        else level.nodes = tail
      } else if (idx == Out.IteratorCursor) {
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
    level.items = Out.EmptyItems
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
      case e: Elem => out.writeClosingTag(e)
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
          out.appendAttrStart(attr.key)
          out.pushAttrValue(attr.js, levelScope)
        } else out.appendJsAttribute(attr.key, attr.js)
      case attr: StringAttr => out.appendAttribute(null, attr.key, attr.value)
      case end: StartTagEnd => endStartTag(end.elem, levelScope, out)
      case other: Node => out.writeRareNode(other, levelScope)
    }
  }

  @inline private def writeElemInline(e: Elem, levelScope: NamespaceBinding, out: Out): Unit = {
    out.append('<')
    out.appendName(e.prefix, e.label)
    e match {
      case fast: ElemOptimized =>
        var cur = fast.attrs
        var pushed = false
        while (cur.nonEmpty && !pushed) {
          val attr = cur.head
          attr._2 match {
            case value: String => out.appendAttribute(null, attr._1, value)
            case js: Js =>
              if (needsLevel(js)) {
                out.pushSeq(startTagRest(cur, fast), levelScope, null)
                pushed = true
              } else out.appendJsAttribute(attr._1, js)
          }
          cur = cur.tail
        }
        if (!pushed) endStartTag(e, levelScope, out)
      case _ =>
        val attrs = e.attributes
        if ((attrs ne null) && (attrs ne Null)) out.appendAttributes(attrs)
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
        out.pushEscaped(enc.js, scope, Out.AttrValueEnd)
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
      out.append(out.codeOf(other))
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
        case js: Js => new JsAttr(attr._1, js)
      }
      i += 1
      cur = cur.tail
    }
    buf(i) = new StartTagEnd(elem)
    ArraySeq.unsafeWrapArray(buf)
  }

  private def endStartTag(e: Elem, levelScope: NamespaceBinding, out: Out): Unit = {
    if ((e.scope ne levelScope) && (e.scope ne TopScope)) {
      out.appendScope(e.scope, levelScope)
    }

    val children = e.child
    children match {
      case n: Node => // a Node that's its own child seq
        if (n.getClass eq classOf[Text]) {
          out.append('>')
          out.escape(n.asInstanceOf[Text].data)
          out.writeClosingTag(e)
        } else if (n.isInstanceOf[Atom[?]]) {
          out.append('>')
          out.escape(n.asInstanceOf[Atom[?]].data.toString)
          out.writeClosingTag(e)
        } else {
          out.append('>')
          out.pushSeq(children, e.scope, e)
        }
      case _ =>
        // Pushed first: then what's in it is read from the level's array (once), not through the Seq's interface.
        out.pushSeq(children, e.scope, e)
        val level = out.levels(out.depth - 1)
        if (level.index >= 0) {
          val len = level.length
          val items = level.items
          if (len == 0) {
            out.dropLevel()
            out.writeEmpty(e)
          } else if (len == 1 && (items(0).getClass eq classOf[Text])) {
            out.dropLevel()
            out.append('>')
            out.escape(items(0).asInstanceOf[Text].data)
            out.writeClosingTag(e)
          } else if (len > 1 && out.spacedAtoms(items, len)) {
            out.dropLevel()
            out.append('>')
            out.writeSpacedAtoms(items, len, e.scope)
            out.writeClosingTag(e)
          } else out.append('>')
        } else {
          // A List or another kind of seq: rare (scala-xml's and fs' children are indexed).
          out.dropLevel()
          if (children.isEmpty) out.writeEmpty(e)
          else {
            out.append('>')
            if (out.spacedAtoms(children)) {
              out.writeSpacedAtoms(children, e.scope)
              out.writeClosingTag(e)
            } else out.pushSeq(children, e.scope, e)
          }
        }
    }
  }

}
