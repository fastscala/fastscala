package com.fastscala.templates.bootstrap5.classes

import com.fastscala.xml.scala_xml.FSScalaXmlSupport

import scala.xml.{Elem, NodeSeq}

trait BasicElemsHelper {

  def Empty: NodeSeq = FSScalaXmlSupport.fsXmlSupport.Empty

  def del: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("del")()

  def s: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("s")()

  def ins: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("ins")()

  def u: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("u")()

  def a: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("a")()

  def small: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("small")()

  def strong: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("strong")()

  def em: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("em")()

  def mark: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("mark")()

  def pre: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("pre")()

  def td: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("td")()

  def tr: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("tr")()

  def tbody: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("tbody")()

  def thead: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("thead")()

  def img: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("img")()

  def div: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("div")()

  def ul: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("ul")()

  def li: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("li")()

  def style: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("style")()

  def input: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("input")()

  def button: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("button")()

  def span: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("span")()

  def label: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("label")()

  def b: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("b")()

  def p: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("p")()

  def abbr: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("abbr")()

  def h1: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("h1")()

  def h2: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("h2")()

  def h3: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("h3")()

  def h4: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("h4")()

  def h5: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("h5")()

  def h6: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("h6")()

  def hr: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("hr")()

  def br: Elem = FSScalaXmlSupport.fsXmlSupport.buildElem("br")()
}

  
