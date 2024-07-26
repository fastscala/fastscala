package com.fastscala.js.rerenderers

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.utils.IdGen

class RerendererP[P, E <: FSXmlEnv : FSXmlSupport](
                                                    renderFunc: RerendererP[P, E] => FSContext => P => E#Elem,
                                                    idOpt: Option[String] = None,
                                                    debugLabel: Option[String] = None,
                                                    gcOldFSContext: Boolean = true
                                                  ) {

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

  def rerender(param: P) = Js.replace(aroundId, render(param)(rootRenderContext.getOrElse(throw new Exception("Missing context - did you call render() first?")))) // & Js(s"""$$("#$aroundId").fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100)""")

  def replaceBy(elem: E#Elem): Js = Js.replace(aroundId, elem.withId(aroundId))

  def replaceContentsBy(elem: E#Elem): Js = Js.setContents(aroundId, elem)

  def map(f: E#Elem => E#Elem) = {
    val out = this
    new RerendererP[P, E](null, None, None) {
      override def render(param: P)(implicit fsc: FSContext): E#Elem = f(out.render(param))

      override def rerender(param: P) = Js.replace(out.aroundId,
        f(out.render(param)(out.rootRenderContext.getOrElse(throw new Exception("Missing context - did you call render() first?"))))) // & Js(s"""$$("#$aroundId").fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100)""")

      override def replaceBy(elem: E#Elem): Js = out.replaceBy(elem)

      override def replaceContentsBy(elem: E#Elem): Js = out.replaceContentsBy(elem)
    }
  }
}
