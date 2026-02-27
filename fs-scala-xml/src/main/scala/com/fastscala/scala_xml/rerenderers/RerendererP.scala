package com.fastscala.scala_xml.rerenderers

import com.fastscala.core.{FSContext, FSPage, FSPageLike}
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.*
import com.fastscala.scala_xml.js.{JS, inScriptTag, printBeforeExec}
import com.fastscala.utils.IdGen

import scala.xml.Elem

class RerendererP[P](renderFunc: RerendererP[P] => FSContext => P => Elem, idOpt: Option[String] = None, debugLabel: Option[String] = None) {

  protected var transforms: Elem => Elem = identity[Elem]

  var aroundId: Option[String] = None

  def getOrGenerateAroundId: String = aroundId.getOrElse({
    aroundId = Some(idOpt.getOrElse(IdGen.id("rerenderer-p")))
    aroundId.get
  })

  protected def renderImpl(param: P)(implicit fsc: FSContext): Elem = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val rendered: Elem = transforms(renderFunc(this)(fsc)(param))
    aroundId = aroundId.orElse(rendered.getIdOpt)
    val renderedWithId: Elem = rendered.withId(getOrGenerateAroundId)
    RerendererDebugStatusState().render(renderedWithId)
  }

  def render(param: P)(implicit fsc: FSContext): Elem = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc => renderImpl(param) }

  def rerender(param: P)(implicit page: FSPageLike) = page.inContextClearedFor(this) { implicit fsc =>
    val rendered: Elem = renderImpl(param)
    val renderedWithId: Elem = rendered.withId(getOrGenerateAroundId)
    RerendererDebugStatusState().rerender(getOrGenerateAroundId, JS.replaceWithScriptExtraction(getOrGenerateAroundId, renderedWithId))
  }

  def replaceBy(elem: Elem): Js = JS.replaceWithScriptExtraction(getOrGenerateAroundId, elem.withId(getOrGenerateAroundId))

  def replaceContentsBy(elem: Elem): Js = JS.setContents(getOrGenerateAroundId, elem)

  def map(f: Elem => Elem): this.type = {
    transforms = transforms andThen f
    this
  }
}
