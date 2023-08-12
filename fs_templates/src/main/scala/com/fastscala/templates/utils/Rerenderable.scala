package com.fastscala.templates.utils

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.utils.Renderable

import scala.xml.NodeSeq

trait Rerenderable extends Renderable with ElemWithRandomId {

  def rerender(): Js = Js.replace(elemId, render())
}