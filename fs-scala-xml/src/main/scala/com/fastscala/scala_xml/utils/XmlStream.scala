package com.fastscala.scala_xml.utils

import java.io.Writer
import scala.annotation.tailrec
import scala.xml.*

/**
 * Writes XML to a [[Writer]] as `Node.toString` would (MinimizeMode.Default), without building the whole
 * string: output is written straight into one fixed `char[]` of [[ChunkSize]] chars (see [[Out]]), handed to the
 * writer each time it fills up. Element names, attributes and text are copied (and escaped) directly into it: no
 * StringBuilder in between (no resizing checks, no Latin-1/UTF-16 coder branches, no copy out of it), except for the
 * rare nodes only scala-xml knows how to write. The writer is not flushed or closed.
 *
 * The output is the same as toString's, except that the attributes of an [[ElemOptimized]] are written straight from
 * its list, which can be in a different order (toString computes them into MetaData first).
 */
object XmlStream {

  val ChunkSize = 8192

  def writeSeq(ns: NodeSeq, w: Writer): Unit = {
    val out = new Out(w)
    writeAll(ns, out)
    out.flush()
  }

  def writeNode(n: Node, w: Writer): Unit = {
    val out = new Out(w)
    writeAll(Seq(n), out)
    out.flush()
  }

  /**
   * The output buffer: a fixed `char[]` of [[ChunkSize]] chars, written to `w` (and reused) whenever it's full, and by
   * [[flush]] at the end. Strings are copied in with String.getChars, split over several chunks if needed.
   * (`buf` and `pos` are public so the inline methods use them directly, without accessors.)
   */
  private final class Out(w: Writer) extends StringBuilderLike {
    val buf = new Array[Char](ChunkSize)
    var pos = 0

    /** For the rare nodes only scala-xml can write (into a StringBuilder): reused, then copied into `buf`. */
    val scratch = new StringBuilder(256)

    def flush(): Unit = if (pos > 0) {
      w.write(buf, 0, pos)
      pos = 0
    }

    inline def append(c: Char): Unit = {
      if (pos == ChunkSize) flush()
      buf(pos) = c
      pos += 1
    }

    override def append(s: String): Unit = append(s, 0, s.length)

    /** `s` from `from` (inclusive) to `to` (exclusive). */
    override def append(s: String, from: Int, to: Int): Unit = {
      val n = to - from
      if (n <= ChunkSize - pos) { // the usual case: it fits
        s.getChars(from, to, buf, pos)
        pos += n
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

    /** What's in `scratch` (then cleared). */
    def appendScratch(): Unit = {
      val sb = scratch.underlying
      val len = sb.length
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

    /** `text`, escaped (see [[XmlEscape]]). */
    def escape(text: String): Unit = XmlEscape.escape(text, this)
  }

  inline private def writeClosingTag(closing: Elem, out: Out): Unit = {
    out.append('<')
    out.append('/')
    appendName(closing.prefix, closing.label, out)
    out.append('>')
  }

  private final class Level(val nodes: Seq[Node], var index: Int, val scope: NamespaceBinding, val closing: Elem) {
    @inline def hasNext: Boolean = index < nodes.length

    @inline def next(): Node = {
      val n = nodes(index)
      index += 1
      n
    }
  }

  /** Initial size of the stack of levels; it's doubled whenever a deeper tree needs it. */
  private val InitialStackSize = 64

  private def writeAll(rootNodes: Seq[Node], out: Out): Unit = {
    // The levels being written, an explicit stack (so any depth works, without recursion): stack(stackTop) is current.
    var stack = new Array[Level](InitialStackSize)
    var stackTop = 0
    stack(0) = new Level(rootNodes, 0, TopScope, null)

    inline def push(level: Level): Unit = {
      stackTop += 1
      if (stackTop == stack.length) stack = java.util.Arrays.copyOf(stack, stack.length * 2)
      stack(stackTop) = level
    }

    while (stackTop >= 0) {
      val level = stack(stackTop)
      if (!level.hasNext) {
        if (level.closing ne null) {
          writeClosingTag(level.closing, out)
        }
        stackTop -= 1 // Pop level
      } else {
        level.next() match {
          case e: Elem =>
            out.append('<')
            appendName(e.prefix, e.label, out)
            e match {
              case fast: ElemOptimized =>
                @tailrec def aux(remaining: List[(String, String)] = fast.attrs): Unit = remaining match {
                  case (key, value) :: rest =>
                    appendAttribute(null, key, value, out)
                    aux(rest)
                  case _ =>
                }

                aux()
              case _ => if (e.attributes ne null) appendAttributes(e.attributes, out)
            }
            if (e.scope != TopScope) {
              e.scope.buildString(out.scratch, level.scope)
              out.appendScratch()
            }

            // Same rule as Utility.serialize with MinimizeMode.Default (i.e. Node.toString): only minimize elements written as
            // `<x/>`. Otherwise an empty `<div></div>` would become `<div/>`, which HTML parses as an unclosed <div>.
            if (e.child.isEmpty && e.minimizeEmpty) {
              out.append('/')
              out.append('>')
            } else {
              if (e.child.isEmpty) {
                out.append('>')
                writeClosingTag(e, out)
              } else {
                out.append('>')
                if (spacedAtoms(e.child)) {
                  writeSpacedAtoms(e.child, e.scope, out)
                  writeClosingTag(e, out)
                } else push(new Level(e.child, 0, e.scope, e))
              }
            }

            // Exactly Text, or exactly Atom (what `{value}` in an XML literal becomes when value isn't a Node): both serialize
            // as Utility.escape(data.toString). Subclasses (PCData, Unparsed, ...) override buildString, so they go below.
          case atom: Atom[?] if isPlainText(atom) =>
            out.escape(atom.data.toString)

            // Exactly Unparsed (the JavaScript in <script> tags): written as is, as its buildString does.
          case u: Unparsed if u.getClass eq classOf[Unparsed] =>
            out.append(u.data)

          case special: SpecialNode => // Text subclasses, Comment, PCData, other Unparsed, EntityRef, ProcInstr
            special.buildString(out.scratch) // what Utility.serialize does for these, minus its own StringBuilder
            out.appendScratch()

          case g: Group => // a Group is just a container of nodes
            // Its nodes are written in the Group's own scope (TopScope), as Utility.serialize does: so they redeclare the
            // namespaces they use, even ones the enclosing element declared.
            push(new Level(g.nodes, 0, g.scope, null))
          case other => // any other Node subclass
            Utility.serialize(other, level.scope, out.scratch)
            out.appendScratch()
        }
      }
    }
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
  private def writeSpacedAtoms(children: collection.Seq[Node], scope: NamespaceBinding, out: Out): Unit = {
    var first = true
    children.foreach { n =>
      if (!first) out.append(' ')
      first = false
      n match {
        case atom: Atom[?] if isPlainText(atom) => out.escape(atom.data.toString)
        case u: Unparsed if u.getClass eq classOf[Unparsed] => out.append(u.data)
        case special: SpecialNode =>
          special.buildString(out.scratch)
          out.appendScratch()
        case other =>
          Utility.serialize(other, scope, out.scratch)
          out.appendScratch()
      }
    }
  }

  private inline def appendName(prefix: String, label: String, out: Out): Unit = {
    if (prefix ne null) {
      out.append(prefix)
      out.append(':')
    }
    out.append(label)
  }

  /** ` pre:key="escaped value"` (` key="..."` if `pre` is null). */
  private def appendAttribute(pre: String, key: String, value: String, out: Out): Unit = {
    out.append(' ')
    appendName(pre, key, out)
    out.append('=')
    out.append('"')
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
    case a: Attribute if a.value eq null => // not a real attribute (e.g. the placeholder heading a fast Elem's)
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
    value.lengthCompare(1) == 0 && isPlainText(value.head)

  private inline def isPlainText(node: Node): Boolean = {
    val cls = node.getClass
    (cls eq classOf[Text]) || (cls eq classOf[Atom[?]])
  }
}
