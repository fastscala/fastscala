package com.fastscala.templates.bootstrap5.modals

import com.fastscala.code.FSContext
import com.fastscala.js.{Js, Rerenderer}
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.utils.IdGen

import scala.xml.{Elem, NodeSeq}

object BSModal5Size extends Enumeration {
  val SM = Value("modal-sm")
  val NORMAL = Value("")
  val LG = Value("modal-lg")
  val XL = Value("modal-xl")
}

abstract class BSModal5Base {

  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

  val modalId = IdGen.id("modal")
  val modalContentId = IdGen.id("modal-content")

  def modalSize: BSModal5Size.Value = BSModal5Size.NORMAL

  def transformModalElem(elem: Elem): Elem = elem.modal.fade.withId(modalId).withClass(modalSize.toString)

  def transformModalDialogElem(elem: Elem): Elem = elem.modal_dialog

  def transformModalContentElem(elem: Elem): Elem = elem.modal_content.withId(modalContentId)

  def transformModalHeaderElem(elem: Elem): Elem = elem.modal_header

  def transformModalBodyElem(elem: Elem): Elem = elem.modal_body

  def transformModalFooterElem(elem: Elem): Elem = elem.modal_footer

  lazy val modalRenderer: Rerenderer = Js.rerenderable(_ => implicit fsc => renderModal())

  lazy val modalContentsRenderer: Rerenderer = Js.rerenderable(_ => implicit fsc => renderModalContent())

  lazy val modalContentsFooterRenderer: Rerenderer = Js.rerenderable(_ => implicit fsc => renderModalFooterContent())

  def install()(implicit fsc: FSContext): Js = Js.append2Body(renderModal())

  def installAndShow(
                      backdrop: Boolean = true
                      , backdropStatic: Boolean = false
                      , focus: Boolean = true
                      , keyboard: Boolean = true
                    )(implicit fsc: FSContext): Js =
    install() & show(backdrop, backdropStatic, focus, keyboard) & show() & removeOnHidden()

  def show(
            backdrop: Boolean = true
            , backdropStatic: Boolean = false
            , focus: Boolean = true
            , keyboard: Boolean = true
          )(implicit fsc: FSContext): Js = Js(
    s""";new bootstrap.Modal(document.getElementById('$modalId'));"""
  )

  def dispose(): Js = Js(s"""$$('#$modalId').modal('dispose')""")

  def remove(): Js = Js.removeId(modalId)

  def handleUpdate(): Js = Js(s"""$$('#$modalId').modal('handleUpdate')""")

  def hideAndRemove(): Js = hide() & onHidden(remove())

  def hide(): Js = Js(s"""$$('#$modalId').modal('hide')""")

  def show(): Js = Js(s"""$$('#$modalId').modal('show')""")

  def toggle(): Js = Js(s"""$$('#$modalId').modal('toggle')""")

  def onShow(js: Js): Js = Js(s"""$$('#$modalId').on('show.bs.modal', function (e) {${js.cmd}});""")

  def onShown(js: Js): Js = Js(s"""$$('#$modalId').on('shown.bs.modal', function (e) {${js.cmd}});""")

  def onHide(js: Js): Js = Js(s"""$$('#$modalId').on('hide.bs.modal', function (e) {${js.cmd}});""")

  def onHidden(js: Js): Js = Js(s"""$$('#$modalId').on('hidden.bs.modal', function (e) {${js.cmd}});""")

  def removeOnHidden(): Js = onHidden(remove())

  def modalHeaderTitle: String

  def modalHeaderContents()(implicit fsc: FSContext): NodeSeq = {
    <h1 class="modal-title fs-5">{modalHeaderTitle}</h1>
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
      }
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
          div.apply {
            modalContentsRenderer.render()
          }
        }
      }
    }
  }
}

object BSModal5 {

  def verySimple(
                  title: String,
                  closeBtnText: String,
                  onHidden: Js = Js.void
                )(
                  contents: FSContext => NodeSeq
                )(implicit fsc: FSContext): Js = {
    val modal = new BSModal5Base {
      override def modalHeaderTitle: String = title

      override def modalBodyContents()(implicit fsc: FSContext): NodeSeq = contents(fsc)

      override def modalFooterContents()(implicit fsc: FSContext): Option[NodeSeq] = Some(BSBtn.BtnPrimary.lbl(closeBtnText).onclick(hide()).btn)
    }
    modal.installAndShow() & modal.onHidden(onHidden)
  }
}
