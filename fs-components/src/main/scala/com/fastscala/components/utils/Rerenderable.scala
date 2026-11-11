package com.fastscala.components.utils

import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.NodeSeq

trait Rerenderable extends ElemWithRandomId {

  def render(): NodeSeq

  def rerender(): Js = JS.replace(elemId, render())
}