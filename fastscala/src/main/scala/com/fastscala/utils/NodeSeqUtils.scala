package com.fastscala.utils

import scala.collection.mutable
import scala.xml.{Elem, NodeSeq, Unparsed}

object NodeSeqUtils {

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

  def main(args: Array[String]): Unit = {
    val rslts = (1 to 1000).map(_ => {
      val start = System.currentTimeMillis()
      val rslt = (1 to 10000).map(_ => {
        <tr>
          <td>Alfreds Futterkiste</td>
          <td>Maria Anders</td>
          <td>Germany</td>
        </tr>
      }).mkNS
      val took = System.currentTimeMillis() - start
      println(took)
      took
    }).sorted
    println(s"${rslts(0)}, ${rslts(99)}, ${rslts(499)}, ${rslts(899)}, ${rslts(999)}")
  }
}
