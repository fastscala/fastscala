package com.fastscala.js.rerenderers

import com.fastscala.core.{FSXmlElem, FSXmlNodeSeq, FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.{Js, JsUtils, JsXmlUtils}
import com.fastscala.core.FSXmlElem
import com.fastscala.utils.IdGen


class Rerenderer[E <: FSXmlEnv](using val env: FSXmlSupport[E])(
  renderFunc: Rerenderer[E] => FSContext => FSXmlElem[E],
  idOpt: Option[String] = None,
  debugLabel: Option[String] = None,
  gcOldFSContext: Boolean = true
) {

  implicit val Js: JsXmlUtils[E] = JsUtils.generic

  import com.fastscala.core.FSXmlUtils.*

  var aroundId = idOpt.getOrElse("around" + IdGen.id)
  var rootRenderContext: Option[FSContext] = None

  def render()(implicit fsc: FSContext): FSXmlElem[E] = {
    rootRenderContext = Some(fsc)
    val rendered: FSXmlElem[E] = renderFunc(this)({
      if (gcOldFSContext) fsc.createNewChildContextAndGCExistingOne(this, debugLabel = debugLabel)
      else fsc
    })
    fsc.page.rerendererDebugStatus.render(rendered.getId() match {
      case Some(id) =>
        aroundId = id
        rendered
      case None => rendered.withIdIfNotSet(aroundId)
    })
  }

  def rerender() = rootRenderContext.map(implicit fsc => {
    fsc.page.rerendererDebugStatus.rerender(aroundId, Js.replace(aroundId, elem2NodeSeq(render())))
  }).getOrElse(throw new Exception("Missing context - did you call render() first?"))

  def replaceBy(elem: FSXmlElem[E]): Js = Js.replace(aroundId, elem2NodeSeq(elem.withId(aroundId)))

  def replaceContentsBy(elem: FSXmlElem[E]): Js = Js.setContents(aroundId, elem2NodeSeq(elem))

  def map(f: FSXmlElem[E] => FSXmlElem[E]) = {
    val out = this
    new Rerenderer[E](null, None, None) {
      override def render()(implicit fsc: FSContext): FSXmlElem[E] = f(out.render())

      override def rerender(): Js = Js.replace(out.aroundId,
        elem2NodeSeq(f(out.render()(out.rootRenderContext.getOrElse(throw new Exception("Missing context - did you call render() first?")))))) // & Js(s"""$$("#$aroundId").fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100)""")

      override def replaceBy(elem: FSXmlElem[E]): Js = out.replaceBy(elem)

      override def replaceContentsBy(elem: FSXmlElem[E]): Js = out.replaceContentsBy(elem)
    }
  }
}
