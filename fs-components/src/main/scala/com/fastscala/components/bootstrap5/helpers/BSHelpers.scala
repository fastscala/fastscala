package com.fastscala.components.bootstrap5.helpers

import com.fastscala.scala_xml.ScalaXmlElemUtils
import ScalaXmlElemUtils.RichElem

import scala.xml.Elem

object BSHelpers extends BSClassesHelper[Elem] with BasicElemsHelper {

  override protected def withClass(clas: String): Elem = <div></div>.addClass(clas)

  implicit class RichElemBootstrapClasses(val elem: Elem) extends AnyVal with BSClassesHelper[Elem] with BSColorsHelper[Elem] with BSDataHelper[Elem] with ScalaXmlElemUtils {
    override def withClass(clas: String): Elem = elem.addClass(clas)

    override protected def setAttribute(name: String, value: String): Elem = elem.withAttr((name, value))
  }

  implicit class RichString(val classes: String) extends AnyVal with BSClassesHelper[String] {
    override protected def withClass(clas: String): String = classes.trim + " " + clas.trim
  }

  implicit class RichClassEnrichableMutable[T <: ClassEnrichableMutable](val enrichable: T) extends AnyVal with BSClassesHelper[T] {
    override protected def withClass(clas: String): T = enrichable.addClass(clas)
  }

  implicit class RichAttributeEnrichableMutable[T <: AttrEnrichableMutable](val enrichable: T) extends AnyVal with BSDataHelper[T] {
    override protected def setAttribute(name: String, value: String): T = enrichable.setAttribute(name, value)
  }

  implicit class RichClassEnrichable[R](val enrichable: ClassEnrichableImmutable[R]) extends AnyVal with BSClassesHelper[R] {
    override protected def withClass(clas: String): R = enrichable.addClass(clas)
  }

  implicit class RichAttributeEnrichable[R](val enrichable: AttrEnrichableImmutable[R]) extends AnyVal with BSDataHelper[R] {
    override protected def setAttribute(name: String, value: String): R = enrichable.setAttribute(name, value)
  }
}
