package com.fastscala.js.rerenderers

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.utils.IdGen

import scala.util.chaining.scalaUtilChainingOps

class ContentRerendererP[E <: FSXmlEnv : FSXmlSupport, P](
                                                           renderFunc: ContentRerendererP[E, P] => FSContext => P => E#NodeSeq,
                                                           id: Option[String] = None,
                                                           debugLabel: Option[String] = None,
                                                           gcOldFSContext: Boolean = true
                                                         ) {

  import com.fastscala.core.FSXmlUtils._

  val outterElem: E#Elem = implicitly[FSXmlSupport[E]].buildElem("div")()

  val aroundId = id.getOrElse("around" + IdGen.id)
  var rootRenderContext: Option[FSContext] = None

  def render(param: P)(implicit fsc: FSContext) = {
    rootRenderContext = Some(fsc)
    outterElem.withIdIfNotSet(aroundId).pipe(elem => {
      elem.withContents(renderFunc.apply(this)({
        if (gcOldFSContext) fsc.createNewChildContextAndGCExistingOne(this, debugLabel = debugLabel)
        else fsc
      })(param))
    })
  }

  def rerender(param: P) = Js.replace(aroundId, render(param)(rootRenderContext.getOrElse(throw new Exception("Missing context - did you call render() first?")))) // & Js(s"""$$("#$aroundId").fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100)""")
}
