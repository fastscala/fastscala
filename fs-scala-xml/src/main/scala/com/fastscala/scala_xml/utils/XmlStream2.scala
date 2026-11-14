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
    writeAll(ns, out)
    out.drain()
  }

  def writeNode(n: Node, w: Writer, pscope: NamespaceBinding = TopScope): Unit = {
    val out = new Out(w)
    writeAll(Seq(n), out)
    out.drain()
  }

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

  inline private def writeClosingTag(closing: Elem, sb: StringBuilder): Unit = {
    sb.append("</")
    val prefix = closing.prefix
    if (prefix ne null) {
      sb.append(prefix).append(':')
    }
    sb.append(closing.label).append('>')
  }

  private final class Level(val nodes: Seq[Node], var index: Int, val scope: NamespaceBinding, val closing: Elem) {
    @inline def hasNext: Boolean = index < nodes.length

    @inline def next(): Node = {
      val n = nodes(index)
      index += 1
      n
    }
  }

  private def writeAll(rootNodes: Seq[Node], out: Out): Unit = {
    // Pre-allocate a fixed-size stack array (e.g., depth 256 is usually plenty for XML)
    val stack = new Array[Level](256)
    var stackTop = -1

    // Push initial level
    stackTop = 0
    stack(0) = new Level(rootNodes, 0, TopScope, null)

    while (stackTop >= 0) {
      val level = stack(stackTop)
      if (!level.hasNext) {
        if (level.closing ne null) {
          writeClosingTag(level.closing, out.sb)
          out.drainIfFull()
        }
        stackTop -= 1 // Pop level
      } else {
        level.next() match {
          case e: Elem =>
            val sb = out.sb
            sb.append('<')
            appendName(e.prefix, e.label, sb)
            e match {
              case fast: ElemOptimized =>
                @tailrec def aux(remaining: List[(String, String)] = fast.attrs): Unit = remaining match {
                  case (key, value) :: rest =>
                    sb.append(' ').append(key).append("=\"")
                    XmlEscape.escape(value, sb.underlying)
                    sb.append('"')
                    aux(rest)
                  case _ =>
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
            } else {
              if (e.child.isEmpty) {
                sb.append('>')
                writeClosingTag(e, sb)
                out.drainIfFull()
              } else {
                sb.append('>')
                out.drainIfFull()
                // Push child level to mutable stack
                stackTop += 1
                stack(stackTop) = new Level(e.child, 0, e.scope, e)
              }
            }

            // Exactly Text, or exactly Atom (what `{value}` in an XML literal becomes when value isn't a Node): both serialize
            // as Utility.escape(data.toString). Subclasses (PCData, Unparsed, ...) override buildString, so they go below.
          case atom: Atom[?] if isPlainText(atom) =>
            XmlEscape.escape(atom.data.toString, out.sb.underlying)
            out.drainIfFull()

          case special: SpecialNode => // Text subclasses, Comment, PCData, Unparsed, EntityRef, ProcInstr
            special.buildString(out.sb) // what Utility.serialize does for these, minus its own StringBuilder
            out.drainIfFull()

          case g: Group => // a Group is just a container of nodes
            stackTop += 1
            stack(stackTop) = new Level(g.nodes, 0, level.scope, null)
          case other => // any other Node subclass
            out.sb.append(Utility.serialize(other, level.scope))
            out.drainIfFull()
        }
      }
    }
  }

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
