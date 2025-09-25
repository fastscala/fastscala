package com.fastscala.scala_xml.rerenderers

import com.fastscala.core.*
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.*
import com.fastscala.scala_xml.js.{JS, inScriptTag}
import com.fastscala.utils.IdGen

import scala.xml.Elem

class Rerenderer(renderFunc: Rerenderer => FSContext => (Elem, Js), idOpt: Option[String] = None, debugLabel: Option[String] = None) {

  var aroundId: Option[String] = None

  def getOrGenerateAroundId: String = aroundId.getOrElse({
    aroundId = Some(idOpt.getOrElse(IdGen.id("rerenderer")))
    aroundId.get
  })

  private def renderImpl()(implicit fsc: FSContext): (Elem, Js) = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val (rendered: Elem, setupJs: Js) = renderFunc(this)(fsc)
    aroundId = aroundId.orElse(rendered.getIdOpt)
    val renderedWithId: Elem = rendered.withId(getOrGenerateAroundId)
    (RerendererDebugStatusState().render(renderedWithId), setupJs)
  }

  def render()(implicit fsc: FSContext): Elem = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val (rendered: Elem, setupJs: Js) = renderImpl()
    rendered.withAppendedToContents(setupJs.onDOMContentLoaded.inScriptTag)
  }

  def rerender()(implicit fsc: FSContext) = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val (rendered: Elem, setupJs: Js) = renderImpl()
    RerendererDebugStatusState().rerender(getOrGenerateAroundId, JS.replace(getOrGenerateAroundId, render()) & setupJs)
  }

  def replaceBy(elem: Elem): Js = JS.replace(getOrGenerateAroundId, (elem.withId(getOrGenerateAroundId)))

  def replaceContentsBy(elem: Elem): Js = JS.setContents(getOrGenerateAroundId, (elem))

//  def map(f: Elem => Elem) = {
//    val out = this
//    new Rerenderer(null, None, None) {
//      override def render()(implicit fsc: FSContext): Elem = f(out.render())
//
//      override def rerender(): Js = JS.replace(out.aroundId, f(out.render()(out.rootRenderContext.getOrElse(throw new Exception("Missing context - did you call render() first?")))))
//
//      override def replaceBy(elem: Elem): Js = out.replaceBy(elem)
//
//      override def replaceContentsBy(elem: Elem): Js = out.replaceContentsBy(elem)
//    }
//  }
}
