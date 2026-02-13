package com.fastscala.scala_xml.rerenderers

import com.fastscala.core.*
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.*
import com.fastscala.scala_xml.js.{JS, inScriptTag}
import com.fastscala.utils.IdGen

import scala.xml.Elem

class Rerenderer(renderFunc: Rerenderer => FSContext => Elem, idOpt: Option[String] = None, debugLabel: Option[String] = None)
  extends RerendererP[Unit](rerenderer => fsc => _ => renderFunc(rerenderer.asInstanceOf[Rerenderer])(fsc), idOpt, debugLabel) {

  def render()(implicit fsc: FSContext): Elem = super.render(())

  def rerender()(implicit fsc: FSPageLike) = super.rerender(())
}
