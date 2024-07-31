package com.fastscala.core

object FSXmlUtils {

  implicit def elem2NodeSeq[E <: FSXmlEnv : FSXmlSupport](elem: E#Elem): E#NodeSeq = implicitly[FSXmlSupport[E]].elem2NodeSeq(elem)

  implicit class RichFSXmlElem[E <: FSXmlEnv](elem: E#Elem)(implicit fsXmlSupport: FSXmlSupport[E]) {

    def getId(): Option[String] = fsXmlSupport.attribute(elem, "id")

    def withId(id: String): E#Elem = fsXmlSupport.transformAttribute(elem, "id", _ => id)

    def withIdIfNotSet(id: String): E#Elem = fsXmlSupport.transformAttribute(elem, "id", _.getOrElse(id))

    def withContents[Env <: FSXmlEnv : FSXmlSupport](contents: Env#NodeSeq): E#Elem = fsXmlSupport.transformContents(elem, _ => contents)
  }
}
