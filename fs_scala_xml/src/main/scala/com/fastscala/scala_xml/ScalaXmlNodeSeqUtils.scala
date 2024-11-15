package com.fastscala.scala_xml

import scala.xml.{Elem, NodeSeq, Unparsed}

object ScalaXmlNodeSeqUtils {

  implicit class MkNSFromNodeSeq(val elems: Iterable[NodeSeq]) extends AnyVal {
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

  implicit class MkNSFromElems(val elems: Iterable[Elem]) extends AnyVal {
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

  implicit class ShowNS(val ns: NodeSeq) extends AnyVal {
    def showIf(b: Boolean): NodeSeq = if (b) ns else NodeSeq.Empty
  }
}
