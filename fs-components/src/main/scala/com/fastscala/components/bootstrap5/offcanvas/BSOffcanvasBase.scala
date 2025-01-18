package com.fastscala.components.bootstrap5.offcanvas

import com.fastscala.components.bootstrap5.helpers.ClassEnrichableMutable
import com.fastscala.components.bootstrap5.utils.BSBtn
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.rerenderers.Rerenderer
import com.fastscala.utils.IdGen

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

abstract class BSOffcanvasBase extends ClassEnrichableMutable with Mutable {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  val offcanvasId = IdGen.id("offcanvas")

  var offcanvasClasses = ""

  override def addClass(clas: String): this.type = {
    offcanvasClasses += s" $clas"
    this
  }

  def transformOffcanvasElem(elem: Elem): Elem = elem.offcanvas.withId(offcanvasId).withAttr("tabindex" -> "-1")

  def transformOffcanvasHeaderElem(elem: Elem): Elem = elem.offcanvas_header

  def transformOffcanvasBodyElem(elem: Elem): Elem = elem.offcanvas_body

  lazy val offcanvasRenderer: Rerenderer = JS.rerenderable(_ => implicit fsc => renderOffcanvas(), debugLabel = Some("offcanvas"))

  lazy val offcanvasHeaderRenderer: Rerenderer = JS.rerenderable(_ => implicit fsc => renderOffcanvasHeader(), debugLabel = Some("offcanvas-header"))

  lazy val offcanvasBodyRenderer: Rerenderer = JS.rerenderable(_ => implicit fsc => renderOffcanvasBody(), debugLabel = Some("offcanvas-body"))

  def append2DOM()(implicit fsc: FSContext): Js = JS.append2Body(offcanvasRenderer.render())

  var offcanvasInstalled: Option[String] = None

  def installAndShow(
                      backdrop: Boolean = true
                      , backdropStatic: Boolean = false
                      , scroll: Boolean = true
                      , keyboard: Boolean = true
                    )(implicit fsc: FSContext): Js =
    install(backdrop, backdropStatic, scroll, keyboard) & show() & removeOnHidden()

  def install(
               backdrop: Boolean = true
               , backdropStatic: Boolean = false
               , scroll: Boolean = true
               , keyboard: Boolean = true
             )(implicit fsc: FSContext): Js =
    append2DOM()(using fsc.page.rootFSContext) & {
      offcanvasInstalled = Some(offcanvasId)
      JS(
        s""";new bootstrap.Offcanvas(document.getElementById('$offcanvasId'), {
           |  backdrop: ${if (backdropStatic) "'static'" else backdrop.toString},
           |  keyboard: $keyboard,
           |  scroll: $scroll,
           |});""".stripMargin
      )
    }

  def hideAndRemoveAndDeleteContext()(implicit fsc: FSContext): Js = hide() & removeOnHidden()

  def dispose(): Js = JS(s"""bootstrap.Offcanvas.getOrCreateInstance('#$offcanvasId').dispose()""")

  def hide(): Js = JS(s"""bootstrap.Offcanvas.getOrCreateInstance('#$offcanvasId').hide()""")

  def show(): Js = JS(s"""bootstrap.Offcanvas.getOrCreateInstance('#$offcanvasId').show()""")

  def toggle(): Js = JS(s"""bootstrap.Offcanvas.getOrCreateInstance('#$offcanvasId').toggle()""")

  def onShow(js: Js): Js = JS(s"""$$('#$offcanvasId').on('show.bs.offcanvas', function (e) {${js.cmd}});""")

  def onShown(js: Js): Js = JS(s"""$$('#$offcanvasId').on('shown.bs.offcanvas', function (e) {${js.cmd}});""")

  def onHide(js: Js): Js = JS(s"""$$('#$offcanvasId').on('hide.bs.offcanvas', function (e) {${js.cmd}});""")

  def onHidden(js: Js): Js = JS(s"""$$('#$offcanvasId').on('hidden.bs.offcanvas', function (e) {${js.cmd}});""")

  def onHidePrevented(js: Js): Js = JS(s"""$$('#$offcanvasId').on('hidePrevented.bs.offcanvas', function (e) {${js.cmd}});""")

  def deleteContext()(implicit fsc: FSContext): Unit = fsc.page.deleteContextFor(offcanvasRenderer)

  def removeAndDeleteContext(offcanvasId: String = offcanvasId)(implicit fsc: FSContext): Js = JS.removeId(offcanvasId) & fsc.callback(() => {
    deleteContext()
    JS.void
  })

  def removeOnHidden()(implicit fsc: FSContext): Js =
    offcanvasInstalled.flatMap { offcanvasId =>
      fsc.page.inContextForOption(offcanvasRenderer) { implicit fsc =>
        offcanvasInstalled = None
        onHidden(removeAndDeleteContext(offcanvasId))
      }
    }.getOrElse(JS.void)

  def offcanvasHeaderTitle: String

  def offcanvasHeaderTitleNs: Elem = <h5 class="offcanvas-title">{offcanvasHeaderTitle}</h5>

  def offcanvasHeaderContents()(implicit fsc: FSContext): NodeSeq = {
    offcanvasHeaderTitleNs ++
      <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  }

  def offcanvasBodyContents()(implicit fsc: FSContext): NodeSeq

  def rerenderOffcanvas()(implicit fsc: FSContext): Js = offcanvasRenderer.rerender()

  def rerenderOffcanvasHeader()(implicit fsc: FSContext): Js = offcanvasHeaderRenderer.rerender()

  def rerenderOffcanvasBody()(implicit fsc: FSContext): Js = offcanvasBodyRenderer.rerender()

  def renderOffcanvasHeader()(implicit fsc: FSContext): Elem = {
    transformOffcanvasHeaderElem {
      div.apply {
        offcanvasHeaderContents()
      }
    }
  }

  def renderOffcanvasBody()(implicit fsc: FSContext): Elem = {
    transformOffcanvasBodyElem {
      div.apply {
        offcanvasBodyContents()
      }
    }
  }

  def renderOffcanvas()(implicit fsc: FSContext): Elem = {
    transformOffcanvasElem {
      div.withClass(offcanvasClasses).apply {
        offcanvasHeaderRenderer.render() ++
          offcanvasBodyRenderer.render()
      }
    }
  }
}
