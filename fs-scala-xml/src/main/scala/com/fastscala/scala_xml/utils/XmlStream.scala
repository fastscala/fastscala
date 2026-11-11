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
object XmlStream {

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
        appendName(level.closing.prefix, level.closing.label, out.sb).append('>')
        out.drainIfFull()
      }
      write(rest, out)
    case level :: _ => level.nodes.next() match {
      case e: Elem =>
        val sb = out.sb
        sb.append('<')
        appendName(e.prefix, e.label, sb)
        e match {
          // Write the attributes straight from its Map: reading e.attributes would build (and cache) the MetaData.
          case fast: ElemWithFastAttrsV3 => appendFastAttrsV3(fast.attrs, sb)
          case fast: ElemWithFastAttrsV2 => appendFastAttrs(fast.attrs, sb)
          case _ => if (e.attributes ne null) appendAttributes(e.attributes, sb)
        }
        e.scope.buildString(sb, level.scope)

        // Same rule as Utility.serialize with MinimizeMode.Default (i.e. Node.toString): only minimize elements written as
        // `<x/>`. Otherwise an empty `<div></div>` would become `<div/>`, which HTML parses as an unclosed <div>.
        if (e.child.isEmpty && e.minimizeEmpty) {
          sb.append("/>")
          out.drainIfFull()
          write(stack, out)
        } else {
          sb.append('>')
          out.drainIfFull()
          write(new Level(e.child.iterator, e.scope, e) :: stack, out)
        }

      case g: Group => // a Group is just a container of nodes
        write(new Level(g.nodes.iterator, level.scope, null) :: stack, out)

        // Exactly Text, or exactly Atom (what `{value}` in an XML literal becomes when value isn't a Node): both serialize
        // as Utility.escape(data.toString). Subclasses (PCData, Unparsed, ...) override buildString, so they go below.
      case atom: Atom[?] if isPlainText(atom) =>
        escape(atom.data.toString, out.sb)
        out.drainIfFull()
        write(stack, out)

      case special: SpecialNode => // Text subclasses, Comment, PCData, Unparsed, EntityRef, ProcInstr
        special.buildString(out.sb) // what Utility.serialize does for these, minus its own StringBuilder
        out.drainIfFull()
        write(stack, out)

      case other => // any other Node subclass
        out.sb.append(Utility.serialize(other, level.scope))
        out.drainIfFull()
        write(stack, out)
    }
  }

  // ---------- allocation-free equivalents of scala-xml's own serialization ----------

  /** As Node.nameToString, which builds an interpolated String for every name. */
  private def appendName(prefix: String, label: String, sb: StringBuilder): StringBuilder = {
    if (prefix ne null) sb.append(prefix).append(':')
    sb.append(label)
  }

  /**
   * As MetaData.buildString, which for each attribute creates two interpolated Strings, a temporary StringBuilder and
   * its String. Attributes whose value is a single [[Text]] or [[Atom]] (nearly all) are written directly; anything else
   * (other value nodes, other MetaData types) goes through scala-xml for that one attribute.
   */
  private def appendAttributes(attributes: MetaData, sb: StringBuilder): Unit = {
    var md = attributes
    while (md ne Null) {
      md match {
        case a: Attribute if a.value eq null => // not an attribute (Elem's normalize drops these), e.g. the placeholder
                                                // heading an ElemWithPendingAttrTransforms' attributes: write nothing
        case a: Attribute if isSinglePlainText(a.value) =>
          sb.append(' ') // like MetaData.buildString
          if (a.isPrefixed) sb.append(a.pre).append(':')
          sb.append(a.key).append("=\"")
          // The escaped text can't contain '"' (it becomes &quot;), so Utility.appendQuoted would pick '"' too.
          escape(a.value.head.asInstanceOf[Atom[?]].data.toString, sb)
          sb.append('"')
        case other =>
          other.copy(Null).buildString(sb) // just this attribute, including its leading ' '
      }
      md = md.next
    }
  }

  /**
   * The attributes of an [[ElemWithFastAttrsV2]], in the same order and format as its computed MetaData would print
   * (see its Interceptor): ` name="escaped value"` for each entry. foreachEntry: no tuple per attribute.
   */
  private def appendFastAttrs(attrs: Map[String, String], sb: StringBuilder): Unit =
    attrs.foreachEntry((name, value) =>
      if (value ne null) { // removed attributes are dropped from the Map, but don't print one if it's there
        sb.append(' ').append(name).append("=\"")
        escape(value, sb)
        sb.append('"')
      })

  /**
   * The attributes of an [[ElemWithFastAttrsV3]], in the same order and format as its computed MetaData would print:
   * ` name="..."`, with each of the value's pieces escaped straight into the buffer, so the value is never joined.
   * Pieces are stored newest first, so they're written from the end of the list.
   */
  private def appendFastAttrsV3(attrs: Map[String, List[String]], sb: StringBuilder): Unit =
    attrs.foreachEntry((name, pieces) =>
      if (pieces.nonEmpty) { // an empty list is a removed attribute (never in the Map, but don't print one if it is)
        sb.append(' ').append(name).append("=\"")
        escapeOldestFirst(pieces, sb)
        sb.append('"')
      })

  /** Escapes `pieces` (newest first) oldest first. Recursion depth is the number of pieces, a handful per value. */
  private def escapeOldestFirst(pieces: List[String], sb: StringBuilder): Unit =
    if (pieces.nonEmpty) {
      escapeOldestFirst(pieces.tail, sb)
      escape(pieces.head, sb)
    }

  private def isPlainText(node: Node): Boolean = {
    val cls = node.getClass
    (cls eq classOf[Text]) || (cls eq classOf[Atom[?]])
  }

  private def isSinglePlainText(value: Seq[Node]): Boolean =
    (value ne null) && value.lengthCompare(1) == 0 && isPlainText(value.head)

  /**
   * Same output as [[Utility.escape]] (`&`, `<`, `>` and `"` become entities; control characters other than \n, \r
   * and \t are dropped), but appending unchanged runs in bulk instead of a Map lookup per character.
   */
  def escape(text: String, sb: StringBuilder): StringBuilder = {
    val jsb = sb.underlying
    val len = text.length
    var runStart = 0
    var i = 0
    while (i < len) {
      val c = text.charAt(i)
      val replacement: String =
        if (c >= ' ') c match {
          case '&' => "&amp;"
          case '<' => "&lt;"
          case '>' => "&gt;"
          case '"' => "&quot;"
          case _ => null
        }
        else if (c == '\n' || c == '\r' || c == '\t') null
        else "" // dropped
      if (replacement ne null) {
        jsb.append(text, runStart, i).append(replacement)
        runStart = i + 1
      }
      i += 1
    }
    jsb.append(text, runStart, len)
    sb
  }
}
