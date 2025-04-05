package com.fastscala.scala_xml.rerenderers

import com.fastscala.core.*
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.{JS, inScriptTag}
import com.fastscala.utils.IdGen
import com.fastscala.scala_xml.ScalaXmlElemUtils.*
import RerendererDebugStatus.RichValue

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

class ContentRerenderer(
                         renderFunc: ContentRerenderer => FSContext => (NodeSeq, Js),
                         id: Option[String] = None,
                         debugLabel: Option[String] = None,
                       ) {

  val outterElem: Elem = <div></div>

  val aroundId = id.getOrElse("around" + IdGen.id)

  private def renderImpl()(implicit fsc: FSContext): (Elem, Js) = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val (rendered: NodeSeq, setupJs: Js) = renderFunc(this)(fsc)
    val renderedWithId: Elem = outterElem.withIdIfNotSet(aroundId).withContents(rendered)
    (RerendererDebugStatusState().render(renderedWithId), setupJs)
  }

  def render()(implicit fsc: FSContext): Elem = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val (rendered: Elem, setupJs: Js) = renderImpl()
    rendered.withAppendedToContents(setupJs.onDOMContentLoaded.inScriptTag)
  }

  def rerender()(implicit fsc: FSContext) = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val (rendered: Elem, setupJs: Js) = renderImpl()
    RerendererDebugStatusState().rerender(aroundId, JS.replace(aroundId, render()) & setupJs)
  }
}
