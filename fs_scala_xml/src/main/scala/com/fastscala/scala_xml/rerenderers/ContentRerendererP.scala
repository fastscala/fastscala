package com.fastscala.scala_xml.rerenderers

import com.fastscala.core.*
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.utils.IdGen
import com.fastscala.scala_xml.ScalaXmlElemUtils.*

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

class ContentRerendererP[P](
                             renderFunc: ContentRerendererP[P] => FSContext => P => NodeSeq,
                             id: Option[String] = None,
                             debugLabel: Option[String] = None,
                           ) {

  val outterElem: Elem = <div></div>

  val aroundId = id.getOrElse("around" + IdGen.id)
  var rootRenderContext: Option[FSContext] = None

  def render(param: P)(implicit fsc: FSContext) = {
    rootRenderContext = Some(fsc)
    RerendererDebugStatusState().render(outterElem.withIdIfNotSet(aroundId).pipe(elem => {
      elem.withContents(fsc.inNewChildContextFor(this, debugLabel = debugLabel)(renderFunc.apply(this)(_)(param)))
    }))
  }

  def rerender(param: P) = rootRenderContext.map(implicit fsc => {
    RerendererDebugStatusState().rerender(aroundId, JS.replace(aroundId, (render(param))))
  }).getOrElse(throw new Exception("Missing context - did you call render() first?"))
}
