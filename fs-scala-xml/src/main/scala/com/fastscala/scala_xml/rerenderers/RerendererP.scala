package com.fastscala.scala_xml.rerenderers

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.*
import com.fastscala.scala_xml.js.{JS, inScriptTag, printBeforeExec}
import com.fastscala.utils.IdGen

import scala.xml.Elem

class RerendererP[P](renderFunc: RerendererP[P] => FSContext => P => (Elem, Js), idOpt: Option[String] = None, debugLabel: Option[String] = None) {

  var aroundId: Option[String] = None

  def getOrGenerateAroundId: String = aroundId.getOrElse({
    aroundId = Some(IdGen.id("rerenderer-p"))
    aroundId.get
  })

  private def renderImpl(param: P)(implicit fsc: FSContext): (Elem, Js) = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val (rendered: Elem, setupJs: Js) = renderFunc(this)(fsc)(param)
    aroundId = aroundId.orElse(rendered.getId)
    val renderedWithId: Elem = rendered.withId(getOrGenerateAroundId)
    (RerendererDebugStatusState().render(renderedWithId), setupJs)
  }

  def render(param: P)(implicit fsc: FSContext): Elem = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val (rendered: Elem, setupJs: Js) = renderImpl(param)
    rendered.withAppendedToContents(setupJs.onDOMContentLoaded.inScriptTag)
  }

  def renderedWithSetupJs(param: P)(implicit fsc: FSContext): (Elem, Js) = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val (rendered: Elem, setupJs: Js) = renderImpl(param)
    (rendered, setupJs)
  }

  def rerender(param: P)(implicit fsc: FSContext) = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel) { implicit fsc =>
    val (rendered: Elem, setupJs: Js) = renderImpl(param)
    RerendererDebugStatusState().rerender(getOrGenerateAroundId, JS.replace(getOrGenerateAroundId, rendered) & setupJs)
  }

  def replaceBy(elem: Elem): Js = JS.replace(getOrGenerateAroundId, elem.withId(getOrGenerateAroundId))

  def replaceContentsBy(elem: Elem): Js = JS.setContents(getOrGenerateAroundId, elem)

//  def map(f: Elem => Elem) = {
//    val out = this
//    new RerendererP[P](null, None, None) {
//      override def render(param: P)(implicit fsc: FSContext): Elem = f(out.render(param))
//
//      override def rerender(param: P) = JS
//        .replace(
//          out.aroundId,
//          (f(out.render(param)(out.rootRenderContext.getOrElse(throw new Exception("Missing context - did you call render() first?")))))
//        ) // & Js(s"""$$("#$aroundId").fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100)""")
//
//      override def replaceBy(elem: Elem): Js = out.replaceBy(elem)
//
//      override def replaceContentsBy(elem: Elem): Js = out.replaceContentsBy(elem)
//    }
//  }
}
