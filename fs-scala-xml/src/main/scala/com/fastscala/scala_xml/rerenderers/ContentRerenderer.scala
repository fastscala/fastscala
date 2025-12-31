package com.fastscala.scala_xml.rerenderers

import com.fastscala.core.*
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.*
import com.fastscala.scala_xml.js.{JS, inScriptTag}
import com.fastscala.scala_xml.rerenderers.RerendererDebugStatus.RichValue
import com.fastscala.utils.IdGen

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

class ContentRerenderer(
                         renderFunc: ContentRerenderer => FSContext => (NodeSeq, Js),
                         idOpt: Option[String] = None,
                         debugLabel: Option[String] = None,
                       ) {
  private var transforms: NodeSeq => NodeSeq = identity[NodeSeq]

  val outerElem: Elem = <div></div>

  val aroundId = idOpt.getOrElse(IdGen.id("around"))

  private def renderImpl()(implicit fsc: FSContext): (Elem, Js) = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val (rendered: NodeSeq, setupJs: Js) = renderFunc(this)(fsc)
    val renderedWithTransforms = transforms(rendered)
    val renderedWithId: Elem = outerElem.withIdIfNotSet(aroundId).withContents(renderedWithTransforms)
    (RerendererDebugStatusState().render(renderedWithId), setupJs)
  }

  def render()(implicit fsc: FSContext): Elem = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val (rendered: Elem, setupJs: Js) = renderImpl()
    if (setupJs.isVoid) rendered else rendered.withAppendedToContents(setupJs.onDOMContentLoaded.inScriptTag)
  }

  def rerender()(implicit fsc: FSContext) = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val (rendered: Elem, setupJs: Js) = renderImpl()
    RerendererDebugStatusState().rerender(aroundId, JS.replace(aroundId, render()) & setupJs)
  }

  def map(f: NodeSeq => NodeSeq): this.type = {
    transforms = transforms andThen f
    this
  }
}
