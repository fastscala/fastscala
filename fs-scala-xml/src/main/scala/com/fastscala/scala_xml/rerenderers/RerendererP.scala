package com.fastscala.scala_xml.rerenderers

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.utils.IdGen
import com.fastscala.scala_xml.ScalaXmlElemUtils.*

import scala.xml.Elem

class RerendererP[P](
                      renderFunc: RerendererP[P] => FSContext => P => Elem,
                      idOpt: Option[String] = None,
                      debugLabel: Option[String] = None,
                    ) {

  var aroundId = idOpt.getOrElse("around" + IdGen.id)
  var rootRenderContext: Option[FSContext] = None

  def render(param: P)(implicit fsc: FSContext): Elem = {
    rootRenderContext = Some(fsc)
    val rendered = fsc.inNewChildContextFor(this, debugLabel = debugLabel)(renderFunc(this)(_)(param))
    RerendererDebugStatusState().render(rendered.getId match {
      case Some(id) =>
        aroundId = id
        rendered
      case None => rendered.withIdIfNotSet(aroundId)
    })
  }

  def rerender(param: P) = rootRenderContext.map(implicit fsc => {
    RerendererDebugStatusState().rerender(aroundId, JS.replace(aroundId, (render(param))))
  }).getOrElse(throw new Exception("Missing context - did you call render() first?"))

  def replaceBy(elem: Elem): Js = JS.replace(aroundId, (elem.withId(aroundId)))

  def replaceContentsBy(elem: Elem): Js = JS.setContents(aroundId, elem)

  def map(f: Elem => Elem) = {
    val out = this
    new RerendererP[P](null, None, None) {
      override def render(param: P)(implicit fsc: FSContext): Elem = f(out.render(param))

      override def rerender(param: P) = JS.replace(out.aroundId,
        (f(out.render(param)(out.rootRenderContext.getOrElse(throw new Exception("Missing context - did you call render() first?")))))) // & Js(s"""$$("#$aroundId").fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100)""")

      override def replaceBy(elem: Elem): Js = out.replaceBy(elem)

      override def replaceContentsBy(elem: Elem): Js = out.replaceContentsBy(elem)
    }
  }
}
