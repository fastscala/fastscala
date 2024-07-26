package com.fastscala.templates.bootstrap5.classes

import com.fastscala.core.{FSXmlEnv, FSXmlSupport}

object BSHelpers extends BSClassesHelper with ElemHelper {

  override def withClass[E <: FSXmlEnv : FSXmlSupport](clas: String): E#Elem =
    com.fastscala.core.FSXmlUtils.EnrichElem(implicitly[FSXmlSupport[E]].buildElem("div")()).addClass(clas)

  implicit class ElemEnricher[E <: FSXmlEnv : FSXmlSupport](val elem: E#Elem) extends BSClassesHelper {
    override def withClass[E <: FSXmlEnv : FSXmlSupport](clas: String): E#Elem =
      com.fastscala.core.FSXmlUtils.EnrichElem(implicitly[FSXmlSupport[E]].buildElem("div")()).addClass(clas)
  }
}
