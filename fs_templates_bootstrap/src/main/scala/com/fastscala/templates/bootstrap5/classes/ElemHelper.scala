package com.fastscala.templates.bootstrap5.classes

import com.fastscala.core.{FSXmlEnv, FSXmlSupport}

trait ElemHelper {

  def Empty[E <: FSXmlEnv : FSXmlSupport]: E#NodeSeq = implicitly[FSXmlSupport[E]].Empty

  def del[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("del")()

  def s[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("s")()

  def ins[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("ins")()

  def u[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("u")()

  def a[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("a")()

  def small[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("small")()

  def strong[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("strong")()

  def em[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("em")()

  def mark[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("mark")()

  def pre[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("pre")()

  def td[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("td")()

  def tr[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("tr")()

  def tbody[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("tbody")()

  def thead[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("thead")()

  def img[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("img")()

  def div[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("div")()

  def ul[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("ul")()

  def li[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("li")()

  def style[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("style")()

  def input[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("input")()

  def button[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("button")()

  def span[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("span")()

  def label[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("label")()

  def b[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("b")()

  def p[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("p")()

  def abbr[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("abbr")()

  def h1[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("h1")()

  def h2[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("h2")()

  def h3[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("h3")()

  def h4[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("h4")()

  def h5[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("h5")()

  def h6[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("h6")()

  def hr[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("hr")()

  def br[E <: FSXmlEnv : FSXmlSupport]: E#Elem = implicitly[FSXmlSupport[E]].buildElem("br")()
}

  
