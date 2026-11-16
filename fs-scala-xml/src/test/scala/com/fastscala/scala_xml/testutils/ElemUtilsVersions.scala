package com.fastscala.scala_xml.testutils

import com.fastscala.scala_xml.xml.ScalaXmlElemUtils

import scala.xml.Elem

/** RichElem for a specific version of the Elem utils, for tests (ScalaXmlElemUtils.RichElem uses only one). */
object ElemUtilsV1 {
  implicit class RichElem(val elem: Elem) extends AnyVal with ScalaXmlElemUtils
}
