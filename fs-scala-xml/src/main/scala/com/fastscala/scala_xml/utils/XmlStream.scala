package com.fastscala.scala_xml.utils

import java.io.Writer
import scala.xml.*

object XmlStream {

  def writeSeq(ns: NodeSeq, w: Writer, pscope: NamespaceBinding = TopScope): Unit =
    ns.foreach(writeNode(_, w, pscope))

  def writeNode(n: Node, w: Writer, pscope: NamespaceBinding = TopScope): Unit = n match {
    case e: Elem =>
      val open = new StringBuilder
      open.append('<')
      e.nameToString(open)
      if (e.attributes ne null) e.attributes.buildString(open)
      e.scope.buildString(open, pscope)

      if (e.child.isEmpty) {
        open.append("/>")
        w.write(open.toString)
      } else {
        open.append('>')
        w.write(open.toString)
        writeSeq(e.child, w, e.scope) // recurse through writeSeq
        w.write("</")
        w.write(e.nameToString(new StringBuilder).toString)
        w.write(">")
      }

    case g: Group => // a Group is just a container of nodes
      writeSeq(g.nodes, w, pscope)

    case other => // Text, Comment, PCData, EntityRef, ...
      w.write(Utility.serialize(other, pscope).toString)
  }
}