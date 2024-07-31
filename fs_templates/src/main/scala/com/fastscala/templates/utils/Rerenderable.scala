package com.fastscala.templates.utils

import com.fastscala.js.Js
import com.fastscala.xml.scala_xml.JS

import scala.xml.NodeSeq

trait Rerenderable extends ElemWithRandomId {

  def render(): NodeSeq

  def rerender(): Js = JS.replace(elemId, render())
}