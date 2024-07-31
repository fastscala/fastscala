package com.fastscala.templates.bootstrap5.classes

import com.fastscala.xml.scala_xml.FSScalaXmlSupport
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.Elem

object BSHelpers extends BSClassesHelper with ElemHelper {

  override protected def withClass(clas: String): Elem =
    FSScalaXmlSupport.fsXmlSupport.buildElem("div")().addClass(clas)

  implicit class ElemEnricher(val elem: Elem) extends BSClassesHelper {
    override protected def withClass(clas: String): Elem =
      elem.addClass(clas)
  }
}
