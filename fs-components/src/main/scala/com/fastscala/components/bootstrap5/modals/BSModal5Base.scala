package com.fastscala.components.bootstrap5.modals

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.rerenderers.Rerenderer
import com.fastscala.components.bootstrap5.helpers.ClassEnrichableMutable
import com.fastscala.components.bootstrap5.utils.BSBtn
import com.fastscala.components.utils.Mutable
import com.fastscala.utils.IdGen
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

abstract class BSModal5Base extends ClassEnrichableMutable with Mutable {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  val modalId = IdGen.id("modal")
  val modalContentId = IdGen.id("modal-content")

  def modalSize: BSModal5Size.Value = BSModal5Size.Normal

  var modalClasses = ""

  override def addClass(clas: String): this.type = {
    modalClasses += s" $clas"
    this
  }

  def transformModalElem(elem: Elem): Elem = elem.modal.fade.withId(modalId)

  def transformModalDialogElem(elem: Elem): Elem = elem.modal_dialog.withClass(modalSize.toString)

  def transformModalContentElem(elem: Elem): Elem = elem.modal_content.withId(modalContentId)

  def transformModalHeaderElem(elem: Elem): Elem = elem.modal_header

  def transformModalBodyElem(elem: Elem): Elem = elem.modal_body

  def transformModalFooterElem(elem: Elem): Elem = elem.modal_footer

  lazy val modalRenderer: Rerenderer = JS.rerenderable(_ => implicit fsc => renderModal(), debugLabel = Some("modal"))

  lazy val modalContentsRenderer: Rerenderer = JS.rerenderable(_ => implicit fsc => renderModalContent(), debugLabel = Some("modal-contents"))

  lazy val modalContentsFooterRenderer: Rerenderer = JS.rerenderable(_ => implicit fsc => renderModalFooterContent(), debugLabel = Some("modal-contents-footer"))

  def append2DOM()(implicit fsc: FSContext): Js = JS.append2Body(modalRenderer.render())

  var modalInstalled: Option[String] = None

  def installAndShow(
                      backdrop: Boolean = true
                      , backdropStatic: Boolean = false
                      , focus: Boolean = true
                      , keyboard: Boolean = true
                    )(implicit fsc: FSContext): Js =
    install(backdrop, backdropStatic, focus, keyboard) & show() & removeOnHidden()

  def install(
               backdrop: Boolean = true
               , backdropStatic: Boolean = false
               , focus: Boolean = true
               , keyboard: Boolean = true
             )(implicit fsc: FSContext): Js =
    append2DOM()(using fsc.page.rootFSContext) & {
      modalInstalled = Some(modalId)
      JS(
        s""";new bootstrap.Modal(document.getElementById('$modalId'), {
           |  backdrop: ${if (backdropStatic) "'static'" else backdrop.toString},
           |  keyboard: $keyboard,
           |  focus: $focus,
           |});""".stripMargin
      )
    }

  def dispose(): Js = JS(s"""bootstrap.Modal.getInstance(document.getElementById('$modalId')).dispose()""")

  def deleteContext()(implicit fsc: FSContext): Unit = fsc.page.deleteContextFor(modalRenderer)

  def removeAndDeleteContext(modalId: String = modalId)(implicit fsc: FSContext): Js = JS.removeId(modalId) & fsc.callback(() => {
    deleteContext()
    JS.void
  })

  def handleUpdate(): Js = JS(s"""bootstrap.Modal.getInstance(document.getElementById('$modalId')).handleUpdate()""")

  def hideAndRemoveAndDeleteContext()(implicit fsc: FSContext): Js = hide() & removeOnHidden()

  def hide(): Js = JS(s"""bootstrap.Modal.getInstance(document.getElementById('$modalId')).hide()""")

  def show(): Js = JS(s"""bootstrap.Modal.getInstance(document.getElementById('$modalId')).show()""")

  def toggle(): Js = JS(s"""bootstrap.Modal.getInstance(document.getElementById('$modalId')).toggle()""")

  def onShow(js: Js): Js = JS(s"""document.getElementById('$modalId').addEventListener('show.bs.modal', function (e) {${js.cmd}});""")

  def onShown(js: Js): Js = JS(s"""document.getElementById('$modalId').addEventListener('shown.bs.modal', function (e) {${js.cmd}});""")

  def onHide(js: Js): Js = JS(s"""document.getElementById('$modalId').addEventListener('hide.bs.modal', function (e) {${js.cmd}});""")

  def onHidden(js: Js): Js = JS(s"""document.getElementById('$modalId').addEventListener('hidden.bs.modal', function (e) {${js.cmd}});""")

  def removeOnHidden()(implicit fsc: FSContext): Js =
    modalInstalled.flatMap { modalId =>
      fsc.page.inContextForOption(modalRenderer) { implicit fsc =>
        modalInstalled = None
        onHidden(removeAndDeleteContext(modalId))
      }
    }.getOrElse(JS.void)

  def modalHeaderTitle: String

  def modalHeaderTitleNs: Elem = <h1 class="modal-title fs-5">{modalHeaderTitle}</h1>

  def modalHeaderContents()(implicit fsc: FSContext): NodeSeq = {
    modalHeaderTitleNs ++
      <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
  }

  def modalBodyContents()(implicit fsc: FSContext): NodeSeq

  def modalFooterContents()(implicit fsc: FSContext): Option[NodeSeq]

  def rerenderModal()(implicit fsc: FSContext): Js = modalRenderer.rerender()

  def rerenderModalContent()(implicit fsc: FSContext): Js = modalContentsRenderer.rerender()

  def rerenderModalFooterContent()(implicit fsc: FSContext): Js = modalContentsFooterRenderer.rerender()

  def renderModalFooterContent()(implicit fsc: FSContext): Elem = {
    modalFooterContents().map(contents => {
      transformModalFooterElem {
        div.apply(contents)
      }: Elem
    }).getOrElse(<div style="display:none;"></div>)
  }

  def renderModalContent()(implicit fsc: FSContext): Elem = {
    transformModalContentElem {
      div.apply {
        transformModalHeaderElem {
          div.apply {
            modalHeaderContents()
          }
        } ++
          transformModalBodyElem {
            div.apply {
              modalBodyContents()
            }
          } ++
          modalContentsFooterRenderer.render()
      }
    }
  }

  def renderModal()(implicit fsc: FSContext): Elem = {
    transformModalElem {
      div.withAttr("tabindex" -> "-1") {
        transformModalDialogElem {
          div.withClass(modalClasses).apply {
            modalContentsRenderer.render()
          }
        }
      }
    }
  }
}
