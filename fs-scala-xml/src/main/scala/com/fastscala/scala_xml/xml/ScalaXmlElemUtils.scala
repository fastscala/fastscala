package com.fastscala.scala_xml.xml

import com.fastscala.scala_xml.xml.ScalaXmlElemUtilsV1

import scala.xml.{Elem, NodeSeq}


object ScalaXmlElemUtils {

  implicit class RichElem(val elem: Elem) extends AnyVal with ScalaXmlElemUtilsV1

  def showIf(b: Boolean)(ns: => NodeSeq): NodeSeq = if (b) ns else NodeSeq.Empty
}