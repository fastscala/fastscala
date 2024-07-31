package com.fastscala.demo.docs.components

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.utils.IdGen
import com.fastscala.xml.scala_xml.JS
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

abstract class Widget {

  lazy val widgetId = IdGen.id("widget-")
  lazy val widgetHeaderId = IdGen.id("widget-header")
  lazy val widgetContentsId = IdGen.id("widget-contents")

  lazy val widgetHeaderRenderer = JS.rerenderable(rerenderer => implicit fsc => renderWidgetHeader(), Some(widgetHeaderId))
  lazy val widgetContentsRenderer = JS.rerenderable(rerenderer => implicit fsc => renderWidgetContents(), Some(widgetContentsId))

  def widgetTitle: String

  def widgetTitleNs: NodeSeq = <h4 class="m-0">{widgetTitle}</h4>

  def widgetTopRight()(implicit fsc: FSContext): NodeSeq = NodeSeq.Empty

  def widgetContents()(implicit fsc: FSContext): NodeSeq

  def rerenderWidget()(implicit fsc: FSContext): Js = JS.replace(widgetId, renderWidget())

  def rerenderWidgetHeader()(implicit fsc: FSContext): Js = widgetHeaderRenderer.rerender()

  def rerenderWidgetContents()(implicit fsc: FSContext): Js = widgetContentsRenderer.rerender()

  def transformWidgetCardHeader(elem: Elem): Elem = elem

  def transformWidgetCardBody(elem: Elem): Elem = elem

  def transformWidgetCard(elem: Elem): Elem = elem

  def renderWidgetHeader()(implicit fsc: FSContext): Elem = {
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    card_header.d_flex.justify_content_between.align_items_center.apply {
      widgetTitleNs ++ <div>{widgetTopRight}</div>
    } pipe transformWidgetCardHeader
  }

  def renderWidgetContents()(implicit fsc: FSContext): Elem = {
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    card_body.apply {
      widgetContents()
    } pipe transformWidgetCardBody
  }

  def renderWidget()(implicit fsc: FSContext): Elem = {
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    card.withId(widgetId).apply {
      widgetHeaderRenderer.render() ++
        widgetContentsRenderer.render()
    } pipe transformWidgetCard
  }
}
