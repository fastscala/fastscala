package com.fastscala.scala_xml.rerenderers

import com.fastscala.core.*
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.*
import com.fastscala.scala_xml.js.{JS, inScriptTag}
import com.fastscala.scala_xml.rerenderers.RerendererDebugStatus.RichValue
import com.fastscala.utils.IdGen

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

class ContentRerenderer(renderFunc: ContentRerenderer => FSContext => NodeSeq, idOpt: Option[String] = None, debugLabel: Option[String] = None)
  extends ContentRerendererP[Unit](rerenderer => fsc => _ => renderFunc(rerenderer.asInstanceOf[ContentRerenderer])(fsc), idOpt, debugLabel) {

  def render()(implicit fsc: FSContext): Elem = super.render(())

  def rerender()(implicit fsc: FSPageLike) = super.rerender(())
}
