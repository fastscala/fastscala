package com.fastscala.templates.bootstrap5.classes

import com.fastscala.xml.scala_xml.{FSScalaXmlSupport, ScalaXmlElemUtils}

import scala.xml.Elem

object BSHelpers extends BSClassesHelper[Elem] with BasicElemsHelper {

  implicit class RichElemBasicOps(val elem: Elem) extends ScalaXmlElemUtils

  override protected def withClass(clas: String): Elem =
    FSScalaXmlSupport.fsXmlSupport.buildElem("div")().addClass(clas)

  implicit class RichElemBootstrapClasses(val elem: Elem) extends BSClassesHelper[Elem] {
    override protected def withClass(clas: String): Elem =
      elem.addClass(clas)
  }

  implicit class RichString(val classes: String) extends BSClassesHelper[String] {
    override protected def withClass(clas: String): String = classes.trim + " " + clas.trim
  }
}
