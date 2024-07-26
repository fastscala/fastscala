package com.fastscala.core

import com.fastscala.js.Js

object FSXmlUtils {

  implicit class EnrichElem[E <: FSXmlEnv : FSXmlSupport](elem: E#Elem) {

    def getId(): Option[String] = implicitly[FSXmlSupport[E]].attribute(elem, "id")

    def getClassAttr: String = implicitly[FSXmlSupport[E]].attribute(elem, "class").getOrElse("")

    def getStyleAttr: String = implicitly[FSXmlSupport[E]].attribute(elem, "style").getOrElse("")

    def getStyle: String = implicitly[FSXmlSupport[E]].attribute(elem, "style").getOrElse("")

    def withId(id: String): E#Elem = implicitly[FSXmlSupport[E]].transformAttribute(elem, "id", _ => id)

    def withIdIfNotSet(id: String): E#Elem = implicitly[FSXmlSupport[E]].transformAttribute(elem, "id", _.getOrElse(id))

    def withClassIf(bool: Boolean, `class`: String): E#Elem = if (bool) addClass(`class`) else elem

    def withAttrIf(bool: Boolean, kv: (String, String)): E#Elem = if (bool) withAttr(kv) else elem

    def withStyle(style: String): E#Elem = implicitly[FSXmlSupport[E]].transformAttribute(elem, "style", _.map(_ + ";").getOrElse("") + style)

    def withFor(`for`: String): E#Elem = implicitly[FSXmlSupport[E]].transformAttribute(elem, "for", _ => `for`)

    def withType(`type`: String): E#Elem = implicitly[FSXmlSupport[E]].transformAttribute(elem, "type", _ => `type`)

    def withValue(value: String): E#Elem = implicitly[FSXmlSupport[E]].transformAttribute(elem, "value", _ => value)

    def withTitle(title: String): E#Elem = implicitly[FSXmlSupport[E]].transformAttribute(elem, "title", _ => title)

    def withName(name: String): E#Elem = implicitly[FSXmlSupport[E]].transformAttribute(elem, "name", _ => name)

    def withTypeSubmit(): E#Elem = implicitly[FSXmlSupport[E]].transformAttribute(elem, "type", _ => "submit")

    def withAttr(name: String)(value: Option[String] => String): E#Elem = implicitly[FSXmlSupport[E]].transformAttribute(elem, name, value)

    def withAttr(kv: (String, String)): E#Elem = withAttr(kv._1)(_ => kv._2)

    def withAttrs(attrs: (String, String)*): E#Elem = attrs.foldLeft[E#Elem](elem)((elem, next) => implicitly[FSXmlSupport[E]].transformAttribute(elem, next._1, _ => next._2))

    def withRole(role: String): E#Elem = implicitly[FSXmlSupport[E]].transformAttribute(elem, "role", _ => role)

    def withHref(href: String): E#Elem = implicitly[FSXmlSupport[E]].transformAttribute(elem, "href", _ => href)

    def withRoleButton: E#Elem = withRole("button")

    def addClass(`class`: String): E#Elem = implicitly[FSXmlSupport[E]].transformAttribute(elem, "class", _.getOrElse("") + " " + `class`)

    def addOnClick(js: String): E#Elem = implicitly[FSXmlSupport[E]].transformAttribute(elem, "onclick", _.getOrElse("") + ";" + js)

    def addOnClick(js: Js): E#Elem = addOnClick(js.cmd)

    def withContents[Env <: FSXmlEnv : FSXmlSupport](contents: Env#NodeSeq): E#Elem = implicitly[FSXmlSupport[E]].transformContents(elem, _ => contents)

    def withPrependedToContents(prepend: E#NodeSeq): E#Elem = implicitly[FSXmlSupport[E]].transformContents(elem, contents => implicitly[FSXmlSupport[E]].concat(prepend, contents))

    def withAppendedToContents(append: E#NodeSeq): E#Elem = implicitly[FSXmlSupport[E]].transformContents(elem, contents => implicitly[FSXmlSupport[E]].concat(contents, append))

    def ++[Env <: FSXmlEnv : FSXmlSupport](other: Env#NodeSeq): E#NodeSeq = implicitly[FSXmlSupport[E]].concat(elem, implicitly[FSXmlSupport[E]].buildNodeSeqFrom[Env](other))

    def apply[Env <: FSXmlEnv : FSXmlSupport](value: Env#NodeSeq): E#Elem = withContents(value)

    def apply[Env <: FSXmlEnv : FSXmlSupport](values: Env#NodeSeq*): E#Elem = withContents(values.foldLeft(implicitly[FSXmlSupport[Env]].Empty)((acc, n) => implicitly[FSXmlSupport[Env]].concat(acc, n)))

    def apply(text: String): E#Elem = withContents(implicitly[FSXmlSupport[E]].buildText(text))

    def content[Env <: FSXmlEnv : FSXmlSupport](value: Env#NodeSeq): E#Elem = withContents(value)

//    def content[Env <: FSXmlEnv : FSXmlSupport](value: Env#Elem): E#Elem = withContents(value)

    def showIf(bool: Boolean): E#NodeSeq = if (bool) elem else implicitly[FSXmlSupport[E]].Empty

    def getAttrs: List[(String, String)] = implicitly[FSXmlSupport[E]].attributes(elem)

    def asElem[Env <: FSXmlEnv : FSXmlSupport](): Env#Elem = implicitly[FSXmlSupport[Env]].buildElemFrom[E](elem)
  }

  implicit class EnrichNodeSeq[E <: FSXmlEnv : FSXmlSupport](ns: E#NodeSeq) {

    def ++[Env <: FSXmlEnv : FSXmlSupport](other: Env#NodeSeq): E#NodeSeq = implicitly[FSXmlSupport[E]].concat(ns, implicitly[FSXmlSupport[E]].buildNodeSeqFrom[Env](other))
  }

  implicit class EnrichSeqNodeSeq[E <: FSXmlEnv : FSXmlSupport](ns: Seq[E#NodeSeq]) {

    def mkNS(): E#NodeSeq = ns.foldLeft(implicitly[FSXmlSupport[E]].Empty)((acc, n) => implicitly[FSXmlSupport[E]].concat(acc, n))
  }

  def showIf[E <: FSXmlEnv : FSXmlSupport](b: Boolean)(ns: => E#NodeSeq): E#NodeSeq = if (b) ns else implicitly[FSXmlSupport[E]].Empty
}
