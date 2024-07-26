package com.fastscala.xml.scala_xml

import scala.xml.{Elem, NodeSeq, Unparsed}

object ScalaXmlNodeSeqUtils {

  def showIf(b: Boolean)(ns: => NodeSeq) = if (b) ns else NodeSeq.Empty

  implicit class MkNSFromNodeSeq(elems: Iterable[NodeSeq]) {
    def mkNS: NodeSeq = {
      val sb = new StringBuilder()
      elems.foreach(sb append _)
      Unparsed(sb.toString)
    }

    def mkNS(sep: NodeSeq) = {
      val sepStr = sep.toString
      Unparsed(elems.mkString(sepStr))
    }
  }

  implicit class MkNSFromElems(elems: Iterable[Elem]) {
    def mkNS: NodeSeq = {
      val sb = new StringBuilder()
      elems.foreach(sb append _)
      Unparsed(sb.toString)
    }

    def mkNS(sep: NodeSeq) = {
      val sepStr = sep.toString
      Unparsed(elems.mkString(sepStr))
    }
  }

  implicit class ShowNS(ns: NodeSeq) {
    def showIf(b: Boolean): NodeSeq = if (b) ns else NodeSeq.Empty
  }
}
