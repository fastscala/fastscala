package com.fastscala.core

import com.fastscala.core.{FSXmlElem, FSXmlNodeSeq}

object FSXmlUtils {

  implicit def elem2NodeSeq[E <: FSXmlEnv](using env: FSXmlSupport[E])(elem: FSXmlElem[E]): FSXmlNodeSeq[E] = env.elem2NodeSeq(elem)

  implicit class RichFSXmlElem[E <: FSXmlEnv](using val env: FSXmlSupport[E])(elem: FSXmlElem[E]) {

    def getId(): Option[String] = env.attribute(elem, "id")

    def withId(id: String): FSXmlElem[E] = env.transformAttribute(elem, "id", _ => id)

    def withIdIfNotSet(id: String): FSXmlElem[E] = env.transformAttribute(elem, "id", _.getOrElse(id))

    def withContents[E2 <: FSXmlEnv](contents: FSXmlNodeSeq[E2])(using otherEnv: FSXmlSupport[E2]): FSXmlElem[E] = env.transformContents(elem, _ => contents)
  }
}
