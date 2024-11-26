package com.fastscala.scala_xml.rerenderers

import com.fastscala.core.*
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.utils.IdGen
import com.fastscala.scala_xml.ScalaXmlElemUtils.*
import RerendererDebugStatus.RichValue

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

class ContentRerenderer(
                         renderFunc: ContentRerenderer => FSContext => NodeSeq,
                         id: Option[String] = None,
                         debugLabel: Option[String] = None,
                       ) {

  val outterElem: Elem = <div></div>

  val aroundId = id.getOrElse("around" + IdGen.id)
  var rootRenderContext: Option[FSContext] = None

  def render()(implicit fsc: FSContext): Elem = {
    rootRenderContext = Some(fsc)
    RerendererDebugStatusState().render(outterElem.withIdIfNotSet(aroundId).pipe(elem => {
      elem.withContents(fsc.inNewChildContextFor(this, debugLabel = debugLabel)(renderFunc(this)(_)))
    }))
  }

  def rerender() = rootRenderContext.map(implicit fsc => {
    RerendererDebugStatusState().rerender(aroundId, JS.replace(aroundId, (render())))
  }).getOrElse(throw new Exception("Missing context - did you call render() first?"))
}
