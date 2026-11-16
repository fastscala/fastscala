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

object WriterHelpers {

  def writeEmpty(e: Elem, out: Writer): Unit =
    if (e.minimizeEmpty) {
      out.append2('/', '>')
    } else {
      out.append('>')
      writeClosingTag(e, out)
    }

  inline def writeClosingTag(closing: Elem, out: Writer): Unit = {
    out.append2('<', '/')
    appendName(closing.prefix, closing.label, out)
    out.append('>')
  }

  inline def appendName(prefix: String, label: String, out: Writer): Unit = {
    if (prefix ne null) {
      out.append(prefix)
      out.append(':')
    }
    out.append(label)
  }

  def appendAttribute(pre: String, key: String, value: String, out: Writer): Unit = {
    out.append(' ')
    appendName(pre, key, out)
    out.append2('=', '"')
    out.escape(value)
    out.append('"')
  }

  def appendAttrStart(key: String, out: Writer): Unit = {
    out.append(' ')
    out.append(key)
    out.append2('=', '"')
  }

  def appendJsAttribute(key: String, js: Js, out: Writer): Unit = {
    appendAttrStart(key, out)
    escapeJs(js, out)
    out.append('"')
  }

  @tailrec def appendAttributes(attributes: MetaData, out: Writer): Unit = attributes match {
    case Null =>
    case a: Attribute if a.value eq null =>
      appendAttributes(a.next, out)
    case a: Attribute if WriterHelpers.isSinglePlainText(a.value) =>
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
      out.appendScratch(other.copy(Null).buildString)
      appendAttributes(other.next, out)
  }

  def isSinglePlainText(value: collection.Seq[Node]): Boolean = value match {
    case n: Node =>
      val cls = n.getClass
      (cls eq classOf[Text]) || (cls eq classOf[Atom[?]])
    case _ =>
      value.lengthCompare(1) == 0 && {
        val cls = value.head.getClass
        (cls eq classOf[Text]) || (cls eq classOf[Atom[?]])
      }
  }

  def escapeJs(js: Js, out: Writer): Unit = js match {
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

  def codeOf(js: Js): String = {
    val cmd = js.cmd
    if (cmd eq null) "null" else cmd
  }

  @tailrec def appendScope(scope: NamespaceBinding, stop: NamespaceBinding, out: Writer): Unit = {
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

  private def spacedAtoms(items: Array[AnyRef], len: Int): Boolean = {
    var i = 0
    while (i < len) {
      if (!isAtomNotText(items(i))) return false
      i += 1
    }
    true
  }

  @noinline def writeSpacedAtoms(items: Array[AnyRef], len: Int, scope: NamespaceBinding, out: Writer): Unit = {
    var i = 0
    while (i < len) {
      if (i > 0) out.append(' ')
      writeAtomOrRare(items(i).asInstanceOf[Node], scope, out)
      i += 1
    }
  }

  inline private def isAtomNotText(n: AnyRef): Boolean = n.isInstanceOf[Atom[?]] && !n.isInstanceOf[Text]

  @noinline def writeSpacedAtoms(children: collection.Seq[Node], scope: NamespaceBinding, out: Writer): Unit =
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

  inline def writeAtomOrRare(n: Node, scope: NamespaceBinding, out: Writer): Unit = n match {
    case atom: Atom[?] if atom.getClass eq classOf[Atom[?]] => out.escape(atom.data.toString)
    case u: Unparsed if u.getClass eq classOf[Unparsed] => out.append(u.data)
    case other => writeRareNode(other, scope, out)
  }

  @noinline def writeRareNode(node: Node, levelScope: NamespaceBinding, out: Writer): Unit = node match {
    case special: SpecialNode =>
      out.appendScratch(special.buildString)
    case other =>
      out.appendScratch(Utility.serialize(other, levelScope, _))
  }
}
