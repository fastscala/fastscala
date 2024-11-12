package com.fastscala.js.rerenderers

import com.fastscala.core.{FSContext, FSXmlElem, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.{Js, JsUtils, JsXmlUtils}
import com.fastscala.utils.IdGen

class RerendererP[E <: FSXmlEnv, P](using val env: FSXmlSupport[E])(
  renderFunc: RerendererP[E, P] => FSContext => P => FSXmlElem[E],
  idOpt: Option[String] = None,
  debugLabel: Option[String] = None,
  gcOldFSContext: Boolean = true
) {

  implicit val Js: JsXmlUtils[E] = JsUtils.generic

  import com.fastscala.core.FSXmlUtils.*

  var aroundId = idOpt.getOrElse("around" + IdGen.id)
  var rootRenderContext: Option[FSContext] = None

  def render(param: P)(implicit fsc: FSContext): FSXmlElem[E] = {
    rootRenderContext = Some(fsc)
    val rendered = renderFunc(this)({
      if (gcOldFSContext) fsc.createNewChildContextAndGCExistingOne(this, debugLabel = debugLabel)
      else fsc
    })(param)
    fsc.page.rerendererDebugStatus.render(rendered.getId() match {
      case Some(id) =>
        aroundId = id
        rendered
      case None => rendered.withIdIfNotSet(aroundId)
    })
  }

  def rerender(param: P) = rootRenderContext.map(implicit fsc => {
    fsc.page.rerendererDebugStatus.rerender(aroundId, Js.replace(aroundId, elem2NodeSeq(render(param))))
  }).getOrElse(throw new Exception("Missing context - did you call render() first?"))

  def replaceBy(elem: FSXmlElem[E]): Js = Js.replace(aroundId, elem2NodeSeq(elem.withId(aroundId)))

  def replaceContentsBy(elem: FSXmlElem[E]): Js = Js.setContents(aroundId, elem2NodeSeq(elem))

  def map(f: FSXmlElem[E] => FSXmlElem[E]) = {
    val out = this
    new RerendererP[E, P](null, None, None) {
      override def render(param: P)(implicit fsc: FSContext): FSXmlElem[E] = f(out.render(param))

      override def rerender(param: P) = Js.replace(out.aroundId,
        elem2NodeSeq(f(out.render(param)(out.rootRenderContext.getOrElse(throw new Exception("Missing context - did you call render() first?")))))) // & Js(s"""$$("#$aroundId").fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100)""")

      override def replaceBy(elem: FSXmlElem[E]): Js = out.replaceBy(elem)

      override def replaceContentsBy(elem: FSXmlElem[E]): Js = out.replaceContentsBy(elem)
    }
  }
}
