package com.fastscala.components.form7.renderers

import scala.xml.{Elem, NodeSeq}


trait F7FormRenderer {

  def render(contents: NodeSeq): Elem
}
