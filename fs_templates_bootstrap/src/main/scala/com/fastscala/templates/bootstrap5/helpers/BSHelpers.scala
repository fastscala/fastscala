package com.fastscala.templates.bootstrap5.helpers

import com.fastscala.xml.scala_xml.{FSScalaXmlSupport, ScalaXmlElemUtils}

import scala.xml.Elem

object BSHelpers extends BSClassesHelper[Elem] with BasicElemsHelper {

  override protected def withClass(clas: String): Elem =
    FSScalaXmlSupport.fsXmlSupport.buildElem("div")().addClass(clas)

  implicit class RichElemBootstrapClasses(val elem: Elem) extends BSClassesHelper[Elem] with BSDataHelper[Elem] with ScalaXmlElemUtils {
    override def withClass(clas: String): Elem = elem.addClass(clas)

    override protected def setAttribute(name: String, value: String): Elem = elem.withAttr((name, value))
  }

  implicit class RichString(val classes: String) extends BSClassesHelper[String] {
    override protected def withClass(clas: String): String = classes.trim + " " + clas.trim
  }

  implicit class RichClassEnrichableMutable[T <: ClassEnrichableMutable](val enrichable: T) extends BSClassesHelper[T] {
    override protected def withClass(clas: String): T = enrichable.addClass(clas)
  }

  implicit class RichAttributeEnrichableMutable[T <: AttrEnrichableMutable](val enrichable: T) extends BSDataHelper[T] {
    override protected def setAttribute(name: String, value: String): T = enrichable.setAttribute(name, value)
  }

  implicit class RichClassEnrichable[R](val enrichable: ClassEnrichableImmutable[R]) extends BSClassesHelper[R] {
    override protected def withClass(clas: String): R = enrichable.addClass(clas)
  }

  implicit class RichAttributeEnrichable[R](val enrichable: AttrEnrichableImmutable[R]) extends BSDataHelper[R] {
    override protected def setAttribute(name: String, value: String): R = enrichable.setAttribute(name, value)
  }
}
