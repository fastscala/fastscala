package com.fastscala.js.rerenderers

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.{Js, JsUtils, JsXmlUtils}
import com.fastscala.utils.IdGen

class RerendererP[E <: FSXmlEnv : FSXmlSupport, P](
                                                    renderFunc: RerendererP[E, P] => FSContext => P => E#Elem,
                                                    idOpt: Option[String] = None,
                                                    debugLabel: Option[String] = None,
                                                    gcOldFSContext: Boolean = true
                                                  ) {

  implicit val Js: JsXmlUtils[E] = JsUtils.generic

  import com.fastscala.core.FSXmlUtils._

  var aroundId = idOpt.getOrElse("around" + IdGen.id)
  var rootRenderContext: Option[FSContext] = None

  def render(param: P)(implicit fsc: FSContext): E#Elem = {
    rootRenderContext = Some(fsc)
    val rendered = renderFunc(this)({
      if (gcOldFSContext) fsc.createNewChildContextAndGCExistingOne(this, debugLabel = debugLabel)
      else fsc
    })(param)
    rendered.getId() match {
      case Some(id) =>
        aroundId = id
        rendered
      case None => rendered.withIdIfNotSet(aroundId)
    }
  }

  def rerender(param: P) = Js.replace(aroundId, elem2NodeSeq(render(param)(rootRenderContext.getOrElse(throw new Exception("Missing context - did you call render() first?"))))) // & Js(s"""$$("#$aroundId").fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100)""")

  def replaceBy(elem: E#Elem): Js = Js.replace(aroundId, elem2NodeSeq(elem.withId(aroundId)))

  def replaceContentsBy(elem: E#Elem): Js = Js.setContents(aroundId, elem2NodeSeq(elem))

  def map(f: E#Elem => E#Elem) = {
    val out = this
    new RerendererP[E, P](null, None, None) {
      override def render(param: P)(implicit fsc: FSContext): E#Elem = f(out.render(param))

      override def rerender(param: P) = Js.replace(out.aroundId,
        elem2NodeSeq(f(out.render(param)(out.rootRenderContext.getOrElse(throw new Exception("Missing context - did you call render() first?")))))) // & Js(s"""$$("#$aroundId").fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100)""")

      override def replaceBy(elem: E#Elem): Js = out.replaceBy(elem)

      override def replaceContentsBy(elem: E#Elem): Js = out.replaceContentsBy(elem)
    }
  }
}
