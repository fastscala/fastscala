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
import scala.xml.{Atom, Node, Text}

trait Writer {

  def appendScratch(f: StringBuilder => Unit): Unit

  def append(c: Char): Unit

  def append2(c1: Char, c2: Char): Unit

  def append(s: String): Unit

  def escape(text: String): Unit

  final def writeEmpty(e: Elem): Unit =
    if (e.minimizeEmpty) {
      append2('/', '>')
    } else {
      append('>')
      writeClosingTag(e)
    }

  inline final def writeClosingTag(closing: Elem): Unit = {
    append2('<', '/')
    appendName(closing.prefix, closing.label)
    append('>')
  }

  inline final def appendName(prefix: String, label: String): Unit = {
    if (prefix ne null) {
      append(prefix)
      append(':')
    }
    append(label)
  }

  final def appendAttribute(pre: String, key: String, value: String): Unit = {
    append(' ')
    appendName(pre, key)
    append2('=', '"')
    escape(value)
    append('"')
  }

  final def appendAttrStart(key: String): Unit = {
    append(' ')
    append(key)
    append2('=', '"')
  }

  final def appendJsAttribute(key: String, js: Js): Unit = {
    appendAttrStart(key)
    escapeJs(js)
    append('"')
  }

  @tailrec final def appendAttributes(attributes: MetaData): Unit = attributes match {
    case Null =>
    case a: Attribute if a.value eq null =>
      appendAttributes(a.next)
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
      appendAttribute(if (a.isPrefixed) a.pre else null, a.key, textVal)
      appendAttributes(a.next)
    case other =>
      appendScratch(other.copy(Null).buildString)
      appendAttributes(other.next)
  }

  final def isSinglePlainText(value: collection.Seq[Node]): Boolean = value match {
    case n: Node =>
      val cls = n.getClass
      (cls eq classOf[Text]) || (cls eq classOf[Atom[?]])
    case _ =>
      value.lengthCompare(1) == 0 && {
        val cls = value.head.getClass
        (cls eq classOf[Text]) || (cls eq classOf[Atom[?]])
      }
  }

  final def escapeJs(js: Js): Unit = js match {
    case composite: CompositeJs =>
      composite.children match {
        case iseq: collection.IndexedSeq[Js] =>
          val len = iseq.length
          var i = 0
          while (i < len) {
            escapeJs(iseq(i))
            i += 1
          }
        case other => other.foreach(escapeJs(_))
      }
    case other => escape(codeOf(other))
  }

  final def codeOf(js: Js): String = {
    val cmd = js.cmd
    if (cmd eq null) "null" else cmd
  }

  @tailrec final def appendScope(scope: NamespaceBinding, stop: NamespaceBinding): Unit = {
    if ((scope ne null) && (scope ne TopScope) && (scope ne stop)) {
      append(" xmlns")
      val prefix = scope.prefix
      if (prefix ne null) {
        append(':')
        append(prefix)
      }
      append2('=', '"')
      val uri = scope.uri
      if (uri ne null) escape(uri)
      append('"')
      appendScope(scope.parent, stop)
    }
  }

  final def spacedAtoms(children: collection.Seq[Node]): Boolean = children match {
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

  final def spacedAtoms(items: Array[AnyRef], len: Int): Boolean = {
    var i = 0
    while (i < len) {
      if (!isAtomNotText(items(i))) return false
      i += 1
    }
    true
  }

  @noinline final def writeSpacedAtoms(items: Array[AnyRef], len: Int, scope: NamespaceBinding): Unit = {
    var i = 0
    while (i < len) {
      if (i > 0) append(' ')
      writeAtomOrRare(items(i).asInstanceOf[Node], scope)
      i += 1
    }
  }

  inline private def isAtomNotText(n: AnyRef): Boolean = n.isInstanceOf[Atom[?]] && !n.isInstanceOf[Text]

  @noinline final def writeSpacedAtoms(children: collection.Seq[Node], scope: NamespaceBinding): Unit =
    children match {
      case iseq: collection.IndexedSeq[Node] =>
        val len = iseq.length
        var i = 0
        while (i < len) {
          if (i > 0) append(' ')
          writeAtomOrRare(iseq(i), scope)
          i += 1
        }
      case _ =>
        var first = true
        children.foreach { n =>
          if (!first) append(' ')
          first = false
          writeAtomOrRare(n, scope)
        }
    }

  inline final def writeAtomOrRare(n: Node, scope: NamespaceBinding): Unit = n match {
    case atom: Atom[?] if atom.getClass eq classOf[Atom[?]] => escape(atom.data.toString)
    case u: Unparsed if u.getClass eq classOf[Unparsed] => append(u.data)
    case other => writeRareNode(other, scope)
  }

  @noinline final def writeRareNode(node: Node, levelScope: NamespaceBinding): Unit = node match {
    case special: SpecialNode =>
      appendScratch(special.buildString)
    case other =>
      appendScratch(Utility.serialize(other, levelScope, _))
  }
}
