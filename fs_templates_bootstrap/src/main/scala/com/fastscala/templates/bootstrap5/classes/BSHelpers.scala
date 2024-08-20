package com.fastscala.templates.bootstrap5.classes

import com.fastscala.xml.scala_xml.{FSScalaXmlSupport, ScalaXmlElemUtils}

import scala.xml.Elem

object BSHelpers extends BSClassesHelper with ElemHelper {

  implicit class RichElem(val elem: Elem) extends ScalaXmlElemUtils

  override protected def withClass(clas: String): Elem =
    FSScalaXmlSupport.fsXmlSupport.buildElem("div")().addClass(clas)

  implicit class ElemEnricher(val elem: Elem) extends BSClassesHelper {
    override protected def withClass(clas: String): Elem =
      elem.addClass(clas)
  }
}
