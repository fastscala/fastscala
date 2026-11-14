package com.fastscala.scala_xml.utils

import java.io.Writer
import java.util.Arrays
import scala.annotation.tailrec
import scala.collection.immutable.IndexedSeq
import scala.xml.*

object XmlStream2 {

  val ChunkSize = 8192

  private val cachedOut = new ThreadLocal[Out]

  private def acquireOut(w: Writer): Out = {
    val out = cachedOut.get()
    if (out ne null) {
      cachedOut.set(null)
      out.init(w)
      out
    } else {
      new Out(w)
    }
  }

  private def releaseOut(out: Out): Unit = {
    out.reset()
    cachedOut.set(out)
  }

  def writeSeq(ns: NodeSeq, w: Writer): Unit = {
    val out = acquireOut(w)
    try {
      out.pushSeq(ns, TopScope, null)
      writeAll(out)
      out.flush()
    } finally {
      releaseOut(out)
    }
  }

  def writeNode(n: Node, w: Writer): Unit = {
    val out = acquireOut(w)
    try {
      out.pushSingle(n, TopScope)
      writeAll(out)
      out.flush()
    } finally {
      releaseOut(out)
    }
  }

  private[utils] final class Out(private var w: Writer) extends StringBuilderLike {
    val buf = new Array[Char](ChunkSize)
    var pos = 0

    private var _scratch: StringBuilder = null

    def scratch: StringBuilder = {
      if (_scratch eq null) _scratch = new StringBuilder(256)
      _scratch
    }

    // Parallel stack arrays: zero Level object allocations, O(N) for both IndexedSeq and List.
    // indexStack encoding:
    //   >= 0 : IndexedSeq cursor
    //   -1   : non-empty ::[Node] (head/tail mode)
    //   -2   : Iterator[Node] fallback
    //   -3   : single root Node (avoids Seq(n) allocation in writeNode)
    var depth = 0
    var maxDepthUsed = 0
    var nodesStack = new Array[AnyRef](64)
    var indexStack = new Array[Int](64)
    var scopeStack = new Array[NamespaceBinding](64)
    var closingStack = new Array[Elem](64)

    def init(writer: Writer): Unit = {
      w = writer
      pos = 0
      depth = 0
      maxDepthUsed = 0
    }

    def reset(): Unit = {
      w = null
      pos = 0
      depth = 0
      if (maxDepthUsed > 0) {
        Arrays.fill(nodesStack, 0, maxDepthUsed, null)
        Arrays.fill(scopeStack.asInstanceOf[Array[AnyRef]], 0, maxDepthUsed, null)
        Arrays.fill(closingStack.asInstanceOf[Array[AnyRef]], 0, maxDepthUsed, null)
        maxDepthUsed = 0
      }
      if ((_scratch ne null) && _scratch.length > 0) _scratch.setLength(0)
    }

    def flush(): Unit = if (pos > 0) {
      w.write(buf, 0, pos)
      pos = 0
    }

    inline def append(c: Char): Unit = {
      if (pos == ChunkSize) flush()
      buf(pos) = c
      pos += 1
    }

    inline def append2(c1: Char, c2: Char): Unit = {
      if (pos + 2 > ChunkSize) flush()
      val p = pos
      buf(p) = c1
      buf(p + 1) = c2
      pos = p + 2
    }

    override def append(s: String): Unit = append(s, 0, s.length)

    override def append(s: String, from: Int, to: Int): Unit = {
      val n = to - from
      if (n <= ChunkSize - pos) {
        if (n > 0) {
          s.getChars(from, to, buf, pos)
          pos += n
        }
      } else if (n >= ChunkSize) {
        flush()
        w.write(s, from, n)
      } else {
        var start = from
        while (start < to) {
          if (pos == ChunkSize) flush()
          val chunk = math.min(to - start, ChunkSize - pos)
          s.getChars(start, start + chunk, buf, pos)
          pos += chunk
          start += chunk
        }
      }
    }

    def appendScratch(): Unit = {
      val sb = _scratch.underlying
      val len = sb.length
      if (len > 0) {
        var start = 0
        while (start < len) {
          if (pos == ChunkSize) flush()
          val chunk = math.min(len - start, ChunkSize - pos)
          sb.getChars(start, start + chunk, buf, pos)
          pos += chunk
          start += chunk
        }
        sb.setLength(0)
      }
    }

    inline def escape(text: String): Unit = XmlEscape.escape(text, this)

    def pushSingle(n: Node, scope: NamespaceBinding): Unit = {
      nodesStack(0) = n
      indexStack(0) = -3
      scopeStack(0) = scope
      closingStack(0) = null
      depth = 1
      maxDepthUsed = 1
    }

    def pushSeq(nodes: collection.Seq[Node], scope: NamespaceBinding, closing: Elem): Unit = {
      val d = depth
      if (d == nodesStack.length) growStacks()
      nodes match {
        case iseq: IndexedSeq[Node] =>
          nodesStack(d) = iseq
          indexStack(d) = 0
        case cons: ::[Node] =>
          nodesStack(d) = cons
          indexStack(d) = -1
        case Nil =>
          nodesStack(d) = IndexedSeq.empty
          indexStack(d) = 0
        case other =>
          nodesStack(d) = other.iterator
          indexStack(d) = -2
      }
      scopeStack(d) = scope
      closingStack(d) = closing
      val nextDepth = d + 1
      depth = nextDepth
      if (nextDepth > maxDepthUsed) maxDepthUsed = nextDepth
    }

    private def growStacks(): Unit = {
      val newCap = nodesStack.length * 2
      nodesStack = Arrays.copyOf(nodesStack, newCap)
      indexStack = Arrays.copyOf(indexStack, newCap)
      scopeStack = Arrays.copyOf(scopeStack, newCap)
      closingStack = Arrays.copyOf(closingStack, newCap)
    }
  }

  private inline def writeClosingTag(closing: Elem, out: Out): Unit = {
    out.append2('<', '/')
    appendName(closing.prefix, closing.label, out)
    out.append('>')
  }

  private def writeAll(out: Out): Unit = {
    while (out.depth > 0) {
      val d = out.depth - 1
      val idx = out.indexStack(d)
      var nextNode: Node = null

      if (idx >= 0) {
        val seq = out.nodesStack(d).asInstanceOf[IndexedSeq[Node]]
        if (idx < seq.length) {
          out.indexStack(d) = idx + 1
          nextNode = seq(idx)
        }
      } else if (idx == -1) {
        val cons = out.nodesStack(d).asInstanceOf[::[Node]]
        nextNode = cons.head
        val tail = cons.tail
        if (tail.isEmpty) {
          out.nodesStack(d) = IndexedSeq.empty
          out.indexStack(d) = 0
        } else {
          out.nodesStack(d) = tail
        }
      } else if (idx == -2) {
        val it = out.nodesStack(d).asInstanceOf[Iterator[Node]]
        if (it.hasNext) nextNode = it.next()
      } else { // idx == -3 (single root node)
        nextNode = out.nodesStack(d).asInstanceOf[Node]
        out.nodesStack(d) = IndexedSeq.empty
        out.indexStack(d) = 0
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
                  appendAttribute(null, attr._1, attr._2, out)
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
              out.pushSeq(children, e.scope, e)
            }

          case t: Text if t.getClass eq classOf[Text] =>
            out.escape(t.data)

          case atom: Atom[?] if atom.getClass eq classOf[Atom[?]] =>
            out.escape(atom.data.toString)

          case u: Unparsed if u.getClass eq classOf[Unparsed] =>
            out.append(u.data)

          case g: Group =>
            out.pushSeq(g.nodes, levelScope, null)

          case other =>
            writeRareNode(other, levelScope, out)
        }
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

  private def appendAttribute(pre: String, key: String, value: String, out: Out): Unit = {
    out.append(' ')
    appendName(pre, key, out)
    out.append2('=', '"')
    out.escape(value)
    out.append('"')
  }

  private inline def textOf(value: collection.Seq[Node]): String = value match {
    case Seq(Text(v)) => v
    case _ => value.text
  }

  @tailrec private def appendAttributes(attributes: MetaData, out: Out): Unit = attributes match {
    case Null =>
    case a: UnprefixedAttribute =>
      val value = a.value
      if (value ne null) appendAttribute(null, a.key, textOf(value), out)
      appendAttributes(a.next, out)

    case a: PrefixedAttribute =>
      val value = a.value
      if (value ne null) appendAttribute(a.pre, a.key, textOf(value), out)
      appendAttributes(a.next, out)
  }
}