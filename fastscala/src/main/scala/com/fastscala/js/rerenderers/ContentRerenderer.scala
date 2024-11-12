package com.fastscala.js.rerenderers

import com.fastscala.core.{FSXmlElem, FSXmlNodeSeq, FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.rerenderers.RerendererDebugStatus.RichValue
import com.fastscala.js.{Js, JsUtils, JsXmlUtils}
import com.fastscala.utils.IdGen

import scala.util.chaining.scalaUtilChainingOps

class ContentRerenderer[E <: FSXmlEnv](using val env: FSXmlSupport[E])(
  renderFunc: ContentRerenderer[E] => FSContext => FSXmlNodeSeq[E],
  id: Option[String] = None,
  debugLabel: Option[String] = None,
  gcOldFSContext: Boolean = true
) {

  implicit val Js: JsXmlUtils[E] = JsUtils.generic

  import com.fastscala.core.FSXmlUtils.*

  val outterElem: FSXmlElem[E] = env.buildElem("div")()

  val aroundId = id.getOrElse("around" + IdGen.id)
  var rootRenderContext: Option[FSContext] = None

  def render()(implicit fsc: FSContext): FSXmlElem[E] = {
    rootRenderContext = Some(fsc)
    new RichValue[E](fsc.page.rerendererDebugStatus).render(outterElem.withIdIfNotSet(aroundId).pipe(elem => {
      new RichFSXmlElem(elem).withContents(renderFunc(this)({
        if (gcOldFSContext) fsc.createNewChildContextAndGCExistingOne(this, debugLabel = debugLabel)
        else fsc
      }))
    }))
  }

  def rerender() = rootRenderContext.map(implicit fsc => {
    fsc.page.rerendererDebugStatus.rerender(aroundId, Js.replace(aroundId, elem2NodeSeq(render())))
  }).getOrElse(throw new Exception("Missing context - did you call render() first?"))
}
