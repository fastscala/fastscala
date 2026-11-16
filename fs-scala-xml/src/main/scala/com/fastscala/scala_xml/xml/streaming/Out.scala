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
 * A level of the stack (see [[Out.levels]]): the items being read, where the reading is at, and what applies to them.
 * Mutable, and reused: a stack slot's Level is created once, and set again by each push that lands on it.
 */
private[xml] final class Level {
  /** The list, iterator or single node read (see the cursors above); not used when reading `items`. */
  var nodes: AnyRef = null
  /**
   * An indexed seq's elements, read from `index` until `length`: an `ArraySeq.ofRef`'s or a small Vector's own array
   * (see [[VectorArrays]]), or the others' copied into `own`. So each element is an array load, not a call through the IndexedSeq interface (which is megamorphic:
   * Vector, ArraySeq, ArrayBuffer... all reach here).
   */
  var items: Array[AnyRef] = Out.EmptyItems
  /** This slot's buffer for copies of indexed seqs (reused by every push that lands on the slot; grown as needed). */
  var own: Array[AnyRef] = null
  var index = 0
  var length = 0
  var scope: NamespaceBinding = null
  var closing: AnyRef = null
  var markupEsc: Encoder.Escaping = Encoder.Unescaped
  var textEsc: Encoder.Escaping = Encoder.Xml1
  var collector: ArrayBuffer[Js] = null

  def setEmpty(): Unit = {
    nodes = null
    items = Out.EmptyItems
    index = 0
    length = 0
  }
}

object Out {
  // The special values of Level.index (a value >= 0 is the cursor into `items`, an indexed seq's elements):
  /** The level's nodes are a non-empty `::` (List), read head by head: `nodes` holds the remaining list. */
  final val ListCursor = -1
  /** The level's nodes are read with an Iterator (any other kind of seq): `nodes` holds the iterator. */
  final val IteratorCursor = -2
  /** The level is a single node (writeNode's root, or a Node used as its own child seq): `nodes` holds the node. */
  final val SingleNodeCursor = -3
  
  val EmptyItems = new Array[AnyRef](0)
  
  object AttrValueEnd
}

/**
 * Serializes into the buffers of [[NonBlockingChunkedSinkWriter]] (which writes them to the sink): the stack of levels being
 * read, and the escaping and script collector in force. Its producer is [[writeAll]], which pauses between nodes.
 */
private[xml] final class Out(protected val sink: Content.Sink, protected val callback: Callback, protected val endContent: Boolean) extends NonBlockingChunkedSinkWriter {

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

  override protected def produce(): Boolean = XmlJsStream.writeAll(this)

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
    level.index = Out.SingleNodeCursor
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
    pushSingleJs(js, scope, Out.AttrValueEnd, nextMarkup, nextMarkup.nextXml)
  }

  private def pushSingleJs(js: Js, scope: NamespaceBinding, closing: AnyRef, nextMarkup: Encoder.Escaping, nextText: Encoder.Escaping): Unit = {
    val level = push()
    level.nodes = js
    level.index = Out.SingleNodeCursor
    level.scope = scope
    level.closing = closing
    level.markupEsc = nextMarkup
    level.textEsc = nextText
    level.collector = null
    collector = null
    setEscaping(nextMarkup, nextText)
  }

  /** Takes the level just pushed off the stack, as if it had never been pushed (nothing was written for it). */
  def dropLevel(): Unit = {
    val level = levels(depth - 1)
    level.nodes = null
    level.items = Out.EmptyItems
    level.scope = null
    level.closing = null
    level.collector = null
    depth -= 1
  }

  @tailrec private def setNodes(level: Level, nodes: collection.Seq[AnyRef]): Unit = nodes match {
    case node: Node =>
      level.nodes = node
      level.index = Out.SingleNodeCursor
    case arr: ArraySeq.ofRef[?] =>
      level.items = arr.unsafeArray.asInstanceOf[Array[AnyRef]]
      level.index = 0
      level.length = arr.length
    case iseq: collection.IndexedSeq[?] =>
      val vector1 = VectorArrays.arrayOfVector1(iseq) // a small Vector's own array: read in place
      if (vector1 ne null) {
        level.items = vector1
        level.index = 0
        level.length = vector1.length
      } else {
        val len = iseq.length
        var own = level.own
        if ((own eq null) || own.length < len) {
          own = new Array[AnyRef](math.max(len, 16))
          level.own = own
        }
        iseq.asInstanceOf[collection.IndexedSeq[AnyRef]].copyToArray(own, 0, len)
        level.items = own
        level.index = 0
        level.length = len
      }
    case ns: NodeSeq => setNodes(level, ns.theSeq)
    case cons: ::[AnyRef] =>
      level.nodes = cons
      level.index = Out.ListCursor
    case Nil =>
      level.setEmpty()
    case other =>
      level.nodes = other.iterator
      level.index = Out.IteratorCursor
  }
}
