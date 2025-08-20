package com.fastscala.scala_xml.rerenderers

import com.fastscala.core.*
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.*
import com.fastscala.scala_xml.js.{JS, inScriptTag}
import com.fastscala.utils.IdGen

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

class ContentRerendererP[P](renderFunc: ContentRerendererP[P] => FSContext => P => (NodeSeq, Js), idOpt: Option[String] = None, debugLabel: Option[String] = None) {

  val outterElem: Elem = <div></div>

  val aroundId = idOpt.getOrElse(IdGen.id("around"))

  private def renderImpl(param: P)(implicit fsc: FSContext): (Elem, Js) = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val (rendered: NodeSeq, setupJs: Js) = renderFunc(this)(fsc)(param)
    val renderedWithId: Elem = outterElem.withIdIfNotSet(aroundId).withContents(rendered)
    (RerendererDebugStatusState().render(renderedWithId), setupJs)
  }

  def render(param: P)(implicit fsc: FSContext): Elem = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val (rendered: Elem, setupJs: Js) = renderImpl(param)
    rendered.withAppendedToContents(setupJs.onDOMContentLoaded.inScriptTag)
  }

  def rerender(param: P)(implicit fsc: FSContext): Js = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val (rendered: Elem, setupJs: Js) = renderImpl(param)
    RerendererDebugStatusState().rerender(aroundId, JS.replace(aroundId, rendered) & setupJs)
  }
}
