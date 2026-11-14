package com.fastscala.scala_xml.xml

import scala.xml.{Elem, Node, NodeSeq, Unparsed}

object ScalaXmlNodeSeqUtils {

  implicit class MkNSFromNodeSeq(val elems: Iterable[NodeSeq]) extends AnyVal {
    def mkNS: NodeSeq = NodeSeq.fromSeq(elems.flatten.toSeq)

    def mkNS(sep: NodeSeq) = {
      val b = Seq.newBuilder[Node]
      val it = elems.iterator
      if (it.hasNext) {
        b ++= it.next()
        while (it.hasNext) {
          b ++= sep
          b ++= it.next()
        }
      }
      NodeSeq.fromSeq(b.result())
    }
  }

  implicit class MkNSFromElems(val elems: Iterable[Elem]) extends AnyVal {
    def mkNS: NodeSeq = NodeSeq.fromSeq(elems.toSeq)

    def mkNS(sep: NodeSeq) = {
      val b = Seq.newBuilder[Node]
      val it = elems.iterator
      if (it.hasNext) {
        b ++= it.next()
        while (it.hasNext) {
          b ++= sep
          b ++= it.next()
        }
      }
      NodeSeq.fromSeq(b.result())
    }
  }

  implicit class ShowNS(val ns: NodeSeq) extends AnyVal {
    def showIf(b: Boolean): NodeSeq = if (b) ns else NodeSeq.Empty
  }
}
