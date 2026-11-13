package com.fastscala.scala_xml.utils

import java.io.Writer
import scala.annotation.tailrec
import scala.xml.*

/**
 * Writes XML to a [[Writer]] exactly as `Node.toString` would (MinimizeMode.Default), without building the whole
 * string: output goes through one reused buffer, handed to the writer in chunks of about [[ChunkSize]] chars.
 * Element names, attributes and text are written straight into that buffer (see [[appendAttributes]] and [[escape]]).
 * The writer is not flushed or closed.
 */
object XmlStream2 {

  val ChunkSize = 8192

  def writeSeq(ns: NodeSeq, w: Writer, pscope: NamespaceBinding = TopScope): Unit = {
    val out = new Out(w)
    write(new Level(ns.iterator, pscope, null) :: Nil, out)
    out.drain()
  }

  def writeNode(n: Node, w: Writer, pscope: NamespaceBinding = TopScope): Unit = {
    val out = new Out(w)
    write(new Level(Iterator.single(n), pscope, null) :: Nil, out)
    out.drain()
  }

  /** The buffer everything is written to, handed to `w` once it reaches [[ChunkSize]] chars. */
  private final class Out(w: Writer) {
    val sb = new StringBuilder(ChunkSize * 2)
    private var chars = new Array[Char](ChunkSize * 2)

    inline def drainIfFull(): Unit = if (sb.length >= ChunkSize) drain()

    def drain(): Unit = {
      val n = sb.length
      if (n > 0) {
        if (chars.length < n) chars = new Array[Char](n) // a single node bigger than the buffer
        sb.underlying.getChars(0, n, chars, 0)
        w.write(chars, 0, n)
        sb.setLength(0)
      }
    }
  }

  /**
   * Nodes still to write at one level of the tree: the children of an element (or of a Group, or the top-level
   * nodes), the namespace scope they're written in, and the element to close once they're done (null if none).
   */
  private final class Level(val nodes: Iterator[Node], val scope: NamespaceBinding, val closing: Elem)

  /** Depth-first, in document order, with an explicit stack of levels, so it's tail recursive. */
  @tailrec private def write(stack: List[Level], out: Out): Unit = stack match {
    case Nil =>
    case level :: rest if !level.nodes.hasNext =>
      if (level.closing ne null) {
        out.sb.append("</")
        if (level.closing.prefix ne null) {
          out.sb.append(level.closing.prefix)
          out.sb.append(':')
        }
        out.sb.append(level.closing.label)
        out.sb.append('>')
        out.drainIfFull()
      }
      write(rest, out)
    case level :: rest => level.nodes.next() match {
      case e: Elem =>
        val sb = out.sb
        sb.append('<')
        appendName(e.prefix, e.label, sb)
        e match {
          case fast: ElemOptimized =>
            @tailrec def aux(rest: List[(String, String)] = fast.attrs): Unit = if (rest.nonEmpty) {
              sb.append(' ').append(rest.head._1).append("=\"")
              XmlEscape.escape(rest.head._2, sb.underlying)
              sb.append('"')
              aux(rest.tail)
            }

            aux()
          case _ => if (e.attributes ne null) appendAttributes(e.attributes, sb)
        }
        if (e.scope != TopScope) e.scope.buildString(sb, level.scope)

        // Same rule as Utility.serialize with MinimizeMode.Default (i.e. Node.toString): only minimize elements written as
        // `<x/>`. Otherwise an empty `<div></div>` would become `<div/>`, which HTML parses as an unclosed <div>.
        if (e.child.isEmpty && e.minimizeEmpty) {
          sb.append("/>")
          out.drainIfFull()
          write(stack, out)
        } else {
          if (e.child.isEmpty) {
            sb.append('>')
            sb.append("</")
            if (e.prefix ne null) {
              sb.append(e.prefix)
              sb.append(':')
            }
            sb.append(e.label)
            sb.append('>')
            out.drainIfFull()
            write(stack, out) // (not rest: this level's remaining siblings, and its closing tag, are still to write)
          } else {
            sb.append('>')
            out.drainIfFull()
            write(new Level(e.child.iterator, e.scope, e) :: stack, out)
          }
        }

        // Exactly Text, or exactly Atom (what `{value}` in an XML literal becomes when value isn't a Node): both serialize
        // as Utility.escape(data.toString). Subclasses (PCData, Unparsed, ...) override buildString, so they go below.
      case atom: Atom[?] if isPlainText(atom) =>
        XmlEscape.escape(atom.data.toString, out.sb.underlying)
        out.drainIfFull()
        write(stack, out)

      case special: SpecialNode => // Text subclasses, Comment, PCData, Unparsed, EntityRef, ProcInstr
        special.buildString(out.sb) // what Utility.serialize does for these, minus its own StringBuilder
        out.drainIfFull()
        write(stack, out)

      case g: Group => // a Group is just a container of nodes
        write(new Level(g.nodes.iterator, level.scope, null) :: stack, out)

      case other => // any other Node subclass
        out.sb.append(Utility.serialize(other, level.scope))
        out.drainIfFull()
        write(stack, out)
    }
  }

  // ---------- allocation-free equivalents of scala-xml's own serialization ----------

  /** As Node.nameToString, which builds an interpolated String for every name. */
  private inline def appendName(prefix: String, label: String, sb: StringBuilder): StringBuilder = {
    if (prefix ne null) sb.append(prefix).append(':')
    sb.append(label)
  }

  @tailrec private def appendAttributes(attributes: MetaData, sb: StringBuilder): Unit = attributes match {
    case Null =>
    case UnprefixedAttribute(key, value, next) =>
      if (value ne null) {
        sb.append(' ').append(key).append("=\"")
        val textVal = value match {
          case Seq(Text(v)) => v
          case _ => value.text
        }
        XmlEscape.escape(textVal, sb.underlying).append('"')
      }
      appendAttributes(next, sb)

    case PrefixedAttribute(pre, key, value, next) =>
      if (value ne null) {
        sb.append(' ').append(pre).append(':').append(key).append("=\"")
        val textVal = value match {
          case Seq(Text(v)) => v
          case _ => value.text
        }
        XmlEscape.escape(textVal, sb.underlying).append('"')
      }
      appendAttributes(next, sb)
  }

  private inline def isPlainText(node: Node): Boolean = {
    val cls = node.getClass
    (cls eq classOf[Text]) || (cls eq classOf[Atom[?]])
  }
}
