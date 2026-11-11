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
  var renderedWithParam: Option[P] = None

  def getOrGenerateAroundId: String = aroundId.getOrElse({
    aroundId = Some(idOpt.getOrElse(IdGen.id("rerenderer-p")))
    aroundId.get
  })

  // Does not create a context: callers (render/rerender) are responsible for running this in this rerenderer's context.
  protected def renderImpl(param: P)(implicit fsc: FSContext): Elem = {
    val rendered: Elem = transforms(renderFunc(this)(fsc)(param))
    aroundId = aroundId.orElse(rendered.getIdOpt)
    renderedWithParam = Some(param)
    val idToSet = getOrGenerateAroundId
    val renderedWithId: Elem = if (rendered.getIdOpt != Some(idToSet)) rendered.withId(getOrGenerateAroundId) else rendered
    RerendererDebugStatusState().render(renderedWithId)
  }

  def render(param: P)(implicit fsc: FSContext): Elem = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc => renderImpl(param) }

  def rerender(param: P)(implicit fsp: FSPageLike): Js = fsp.inContextClearedFor(this) { implicit fsc =>
    val rendered: Elem = renderImpl(param)
    val idToSet = getOrGenerateAroundId
    val renderedWithId: Elem = if (rendered.getIdOpt != Some(idToSet)) rendered.withId(getOrGenerateAroundId) else rendered
    RerendererDebugStatusState().rerender(getOrGenerateAroundId, JS.replaceWithScriptExtraction(getOrGenerateAroundId, renderedWithId))
  }

  def rerenderTransform(modify: P => P)(implicit fsp: FSPageLike): Js =
    rerender(modify(renderedWithParam.getOrElse(throw new Exception("Called rerender with a function to modify the current params, but hasn't been rendered the first time yet!"))))

  def replaceBy(elem: Elem): Js = JS.replaceWithScriptExtraction(getOrGenerateAroundId, elem.withId(getOrGenerateAroundId))

  def replaceContentsBy(elem: Elem): Js = JS.setContents(getOrGenerateAroundId, elem)

  def map(f: Elem => Elem): this.type = {
    transforms = transforms andThen f
    this
  }
}
