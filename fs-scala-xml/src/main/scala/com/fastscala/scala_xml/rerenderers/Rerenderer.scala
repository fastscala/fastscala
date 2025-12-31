package com.fastscala.scala_xml.rerenderers

import com.fastscala.core.*
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.*
import com.fastscala.scala_xml.js.{JS, inScriptTag}
import com.fastscala.utils.IdGen

import scala.xml.Elem

class Rerenderer(renderFunc: Rerenderer => FSContext => (Elem, Js), idOpt: Option[String] = None, debugLabel: Option[String] = None) {

  private var transforms: Elem => Elem = identity[Elem]

  var aroundId: Option[String] = None

  def getOrGenerateAroundId: String = aroundId.getOrElse({
    aroundId = Some(idOpt.getOrElse(IdGen.id("rerenderer")))
    aroundId.get
  })

  private def renderImpl()(implicit fsc: FSContext): (Elem, Js) = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val (rendered: Elem, setupJs: Js) = renderFunc(this)(fsc)
    val renderedTransformed = transforms(rendered)
    aroundId = aroundId.orElse(renderedTransformed.getIdOpt)
    val renderedWithId: Elem = renderedTransformed.withId(getOrGenerateAroundId)
    (RerendererDebugStatusState().render(renderedWithId), setupJs)
  }

  def render()(implicit fsc: FSContext): Elem = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val (rendered: Elem, setupJs: Js) = renderImpl()
    if(setupJs.isVoid) rendered else rendered.withAppendedToContents(setupJs.onDOMContentLoaded.inScriptTag)
  }

  def rerender()(implicit fsc: FSContext) = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val (rendered: Elem, setupJs: Js) = renderImpl()
    RerendererDebugStatusState().rerender(getOrGenerateAroundId, JS.replace(getOrGenerateAroundId, render()) & setupJs)
  }

  def replaceBy(elem: Elem): Js = JS.replace(getOrGenerateAroundId, (elem.withId(getOrGenerateAroundId)))

  def replaceContentsBy(elem: Elem): Js = JS.setContents(getOrGenerateAroundId, (elem))

  def map(f: Elem => Elem): this.type = {
    transforms = transforms andThen f
    this
  }
}
