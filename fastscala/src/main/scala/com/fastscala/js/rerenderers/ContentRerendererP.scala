package com.fastscala.js.rerenderers

import com.fastscala.core.{FSXmlElem, FSXmlNodeSeq, FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.{Js, JsUtils, JsXmlUtils}
import com.fastscala.utils.IdGen

import scala.util.chaining.scalaUtilChainingOps

class ContentRerendererP[E <: FSXmlEnv, P](using val env: FSXmlSupport[E])(
  renderFunc: ContentRerendererP[E, P] => FSContext => P => FSXmlNodeSeq[E],
  id: Option[String] = None,
  debugLabel: Option[String] = None,
  gcOldFSContext: Boolean = true
) {

  implicit val Js: JsXmlUtils[E] = JsUtils.generic

  import com.fastscala.core.FSXmlUtils.*

  val outterElem: FSXmlElem[E] = env.buildElem("div")()

  val aroundId = id.getOrElse("around" + IdGen.id)
  var rootRenderContext: Option[FSContext] = None

  def render(param: P)(implicit fsc: FSContext) = {
    rootRenderContext = Some(fsc)
    fsc.page.rerendererDebugStatus.render(outterElem.withIdIfNotSet(aroundId).pipe(elem => {
      elem.withContents(renderFunc.apply(this)({
        if (gcOldFSContext) fsc.createNewChildContextAndGCExistingOne(this, debugLabel = debugLabel)
        else fsc
      })(param))
    }))
  }

  def rerender(param: P) = rootRenderContext.map(implicit fsc => {
    fsc.page.rerendererDebugStatus.rerender(aroundId, Js.replace(aroundId, elem2NodeSeq(render(param))))
  }).getOrElse(throw new Exception("Missing context - did you call render() first?"))
}
