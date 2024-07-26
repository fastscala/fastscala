package com.fastscala.js.rerenderers

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.utils.IdGen

import scala.util.chaining.scalaUtilChainingOps

class ContentRerenderer[E <: FSXmlEnv : FSXmlSupport](
                                                       renderFunc: ContentRerenderer[E] => FSContext => E#NodeSeq,
                                                       id: Option[String] = None,
                                                       debugLabel: Option[String] = None,
                                                       gcOldFSContext: Boolean = true
                                                     ) {

  import com.fastscala.core.FSXmlUtils._

  val outterElem: E#Elem = implicitly[FSXmlSupport[E]].buildElem("div")()

  val aroundId = id.getOrElse("around" + IdGen.id)
  var rootRenderContext: Option[FSContext] = None

  def render()(implicit fsc: FSContext): E#Elem = {
    rootRenderContext = Some(fsc)
    outterElem.withIdIfNotSet(aroundId).pipe(elem => {
      elem.withContents(renderFunc(this)({
        if (gcOldFSContext) fsc.createNewChildContextAndGCExistingOne(this, debugLabel = debugLabel)
        else fsc
      }))
    })
  }

  def rerender(): Js = Js.replace(aroundId, render()(rootRenderContext.getOrElse(throw new Exception("Missing context - did you call render() first?")))) // & Js(s"""$$("#$aroundId").fadeOut(100).fadeIn(100).fadeOut(100).fadeIn(100)""")
}
