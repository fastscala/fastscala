package com.fastscala.demo.examples.components

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.utils.IdGen

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

abstract class Widget {

  lazy val widgetId = IdGen.id("widget-")
  lazy val widgetHeaderId = IdGen.id("widget-header")
  lazy val widgetContentsId = IdGen.id("widget-contents")

  lazy val widgetHeaderRenderer = Js.rerenderable(rerenderer => implicit fsc => renderHeader(), Some(widgetHeaderId))
  lazy val widgetContentsRenderer = Js.rerenderable(rerenderer => implicit fsc => renderContents(), Some(widgetContentsId))

  def widgetTitle: String

  def widgetTitleNs: NodeSeq = <h4 class="m-0">{widgetTitle}</h4>

  def widgetTopRight()(implicit fsc: FSContext): NodeSeq = NodeSeq.Empty

  def widgetContents()(implicit fsc: FSContext): NodeSeq

  def rerenderWidget()(implicit fsc: FSContext): Js = Js.replace(widgetId, render())

  def rerenderWidgetHeader()(implicit fsc: FSContext): Js = widgetHeaderRenderer.rerender()

  def rerenderWidgetContents()(implicit fsc: FSContext): Js = widgetContentsRenderer.rerender()

  def transformCardHeader(elem: Elem): Elem = elem

  def transformCardBody(elem: Elem): Elem = elem

  def transformCard(elem: Elem): Elem = elem

  def renderHeader()(implicit fsc: FSContext): Elem = {
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    card_header.d_flex.justify_content_between.align_items_center.apply {
      widgetTitleNs ++ <div>{widgetTopRight}</div>
    } pipe transformCardHeader
  }

  def renderContents()(implicit fsc: FSContext): Elem = {
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    card_body.apply {
      widgetContents()
    } pipe transformCardBody
  }

  def render()(implicit fsc: FSContext): Elem = {
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    card.withId(widgetId).apply {
      widgetHeaderRenderer.render() ++
        widgetContentsRenderer.render()
    } pipe transformCard
  }
}
