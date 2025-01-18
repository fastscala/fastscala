package com.fastscala.scala_xml.rerenderers

import com.fastscala.core.*
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.utils.IdGen
import com.fastscala.scala_xml.ScalaXmlElemUtils.*

import scala.xml.Elem

class Rerenderer(
                  renderFunc: Rerenderer => FSContext => Elem,
                  idOpt: Option[String] = None,
                  debugLabel: Option[String] = None,
                ) {

  var aroundId = idOpt.getOrElse("around" + IdGen.id)
  var rootRenderContext: Option[FSContext] = None

  def render()(implicit fsc: FSContext): Elem = {
    rootRenderContext = Some(fsc)
    val rendered: Elem = fsc.runInNewOrRenewedChildContextFor(this, debugLabel = debugLabel)(renderFunc(this)(_))
    RerendererDebugStatusState().render(rendered.getId match {
      case Some(id) =>
        aroundId = id
        rendered
      case None => rendered.withIdIfNotSet(aroundId)
    })
  }

  def rerender() = rootRenderContext.map(implicit fsc => {
    RerendererDebugStatusState().rerender(aroundId, JS.replace(aroundId, (render())))
  }).getOrElse(throw new Exception("Missing context - did you call render() first?"))

  def replaceBy(elem: Elem): Js = JS.replace(aroundId, (elem.withId(aroundId)))

  def replaceContentsBy(elem: Elem): Js = JS.setContents(aroundId, (elem))

  def map(f: Elem => Elem) = {
    val out = this
    new Rerenderer(null, None, None) {
      override def render()(implicit fsc: FSContext): Elem = f(out.render())

      override def rerender(): Js = JS.replace(out.aroundId,
        (f(out.render()(out.rootRenderContext.getOrElse(throw new Exception("Missing context - did you call render() first?")))))) // & Js(s"""$$("#$aroundId").fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100)""")

      override def replaceBy(elem: Elem): Js = out.replaceBy(elem)

      override def replaceContentsBy(elem: Elem): Js = out.replaceContentsBy(elem)
    }
  }
}
