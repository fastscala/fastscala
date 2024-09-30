package com.fastscala.templates.bootstrap5.helpers

import com.fastscala.xml.scala_xml.{FSScalaXmlSupport, ScalaXmlElemUtils}

import scala.xml.Elem

object BSHelpers extends BSClassesHelper[Elem] with BasicElemsHelper {

  implicit class RichElemBasicOps(val elem: Elem) extends ScalaXmlElemUtils

  override protected def withClass(clas: String): Elem =
    FSScalaXmlSupport.fsXmlSupport.buildElem("div")().addClass(clas)

  implicit class RichElemBootstrapClasses(val elem: Elem) extends BSClassesHelper[Elem] with BSDataHelper[Elem] {
    override protected def withClass(clas: String): Elem = elem.addClass(clas)

    override protected def setAttribute(name: String, value: String): Elem = elem.withAttr((name, value))
  }

  implicit class RichString(val classes: String) extends BSClassesHelper[String] {
    override protected def withClass(clas: String): String = classes.trim + " " + clas.trim
  }

  implicit class RichClassEnrichable[T <: ClassEnrichable](val enrichable: T) extends BSClassesHelper[T] {
    override protected def withClass(clas: String): T = enrichable.setClass(clas)
  }

  implicit class RichAttributeEnrichable[T <: AttrEnrichable](val enrichable: T) extends BSDataHelper[T] {
    override protected def setAttribute(name: String, value: String): T = enrichable.setAttribute(name, value)
  }
}
