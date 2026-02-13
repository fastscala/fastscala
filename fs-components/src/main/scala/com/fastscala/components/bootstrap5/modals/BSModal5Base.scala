package com.fastscala.components.bootstrap5.modals

import com.fastscala.components.bootstrap5.helpers.ClassEnrichableMutable
import com.fastscala.components.bootstrap5.utils.BSBtn
import com.fastscala.components.utils.Mutable
import com.fastscala.core.{FSContext, FSPageLike}
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.rerenderers.Rerenderer
import com.fastscala.utils.IdGen

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

  def transformModalDialogElem(elem: Elem): Elem = elem.modal_dialog.addClass(modalSize.toString)

  def transformModalContentElem(elem: Elem): Elem = elem.modal_content.withId(modalContentId)

  def transformModalHeaderElem(elem: Elem): Elem = elem.modal_header

  def transformModalBodyElem(elem: Elem): Elem = elem.modal_body

  def transformModalFooterElem(elem: Elem): Elem = elem.modal_footer

  lazy val modalRenderer: Rerenderer = JS.rerenderable(_ => implicit fsc => renderModal(), debugLabel = Some("modal"))

  lazy val modalContentsRenderer: Rerenderer = JS.rerenderable(_ => implicit fsc => renderModalContent(), debugLabel = Some("modal-contents"))

  lazy val modalContentsFooterRenderer: Rerenderer = JS.rerenderable(_ => implicit fsc => renderModalFooterContent(), debugLabel = Some("modal-contents-footer"))

  def append2DOM()(implicit fsc: FSContext): Js = JS.append2BodyWithScriptExtraction(modalRenderer.render())

  def installAndShow(
                      backdrop: Boolean = true
                      , backdropStatic: Boolean = false
                      , focus: Boolean = true
                      , keyboard: Boolean = true
                    )(implicit fsc: FSContext): Js =
    install(backdrop, backdropStatic, focus, keyboard) & show()

  def install(
               backdrop: Boolean = true
               , backdropStatic: Boolean = false
               , focus: Boolean = true
               , keyboard: Boolean = true
             )(implicit fsc: FSContext): Js =
    append2DOM()(using fsc.page.rootFSContext) & {
      JS(
        s"""new bootstrap.Modal(document.getElementById('$modalId'), {
           |  backdrop: ${if (backdropStatic) "'static'" else backdrop.toString},
           |  keyboard: $keyboard,
           |  focus: $focus,
           |});""".stripMargin
      ) &
        List(
          Some(eventListenerOnShow()).filter(_ != Js.Void).map(addEventListenerOnShow),
          Some(eventListenerOnShown()).filter(_ != Js.Void).map(addEventListenerOnShown),
          Some(eventListenerOnHide()).filter(_ != Js.Void).map(addEventListenerOnHide),
          Some(eventListenerOnHidden()).filter(_ != Js.Void).map(addEventListenerOnHidden)
        ).flatten.reduceOption(_ & _).getOrElse(Js.Void)
    }

  def dispose(): Js = JS(s"""bootstrap.Modal.getInstance(document.getElementById('$modalId')).dispose()""")

  def deleteContext()(implicit page: FSPageLike): Unit = page.deleteContextFor(modalRenderer)

  def handleUpdate(): Js = JS(s"""bootstrap.Modal.getInstance(document.getElementById('$modalId')).handleUpdate()""")

  def hideAndRemoveAndDeleteContext()(implicit fsc: FSContext): Js = hide()

  def hide(): Js = JS(s"""bootstrap.Modal.getInstance(document.getElementById('$modalId')).hide()""")

  def show(): Js = JS(s"""bootstrap.Modal.getInstance(document.getElementById('$modalId')).show()""")

  def toggle(): Js = JS(s"""bootstrap.Modal.getInstance(document.getElementById('$modalId')).toggle()""")

  def onHideDefaultListenerServerSide()(implicit fsc: FSPageLike): Js = Js.Void

  def eventListenerOnShow()(implicit fsc: FSContext): Js = Js.Void

  def eventListenerOnShown()(implicit fsc: FSContext): Js = Js.Void

  def eventListenerOnHide()(implicit fsc: FSContext): Js =
    JS.removeId(modalId) &
      fsc.callback(() => {
        deleteContext()
        onHideDefaultListenerServerSide()
      })

  def eventListenerOnHidden()(implicit fsc: FSContext): Js = Js.Void

  def addEventListenerOnShow(js: Js): Js = JS(s"""document.getElementById('$modalId').addEventListener('show.bs.modal', function (e) {${js.cmd}});""")

  def addEventListenerOnShown(js: Js): Js = JS(s"""document.getElementById('$modalId').addEventListener('shown.bs.modal', function (e) {${js.cmd}});""")

  def addEventListenerOnHide(js: Js): Js = JS(s"""document.getElementById('$modalId').addEventListener('hide.bs.modal', function (e) {${js.cmd}});""")

  def addEventListenerOnHidden(js: Js): Js = JS(s"""document.getElementById('$modalId').addEventListener('hidden.bs.modal', function (e) {${js.cmd}});""")

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
          div.addClass(modalClasses).apply {
            modalContentsRenderer.render()
          }
        }
      }
    }
  }
}
