package com.fastscala.components.bootstrap5.modals

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

object BSModal5 {

  def verySimple(
                  title: String,
                  closeBtnText: String,
                  onHidden: Js = JS.void,
                  modalFooterButton: BSBtn = BSBtn().BtnPrimary
                )(
                  contents: BSModal5Base => FSContext => NodeSeq
                )(implicit fsc: FSContext): Js = {
    val modal = new BSModal5Base {
      override def modalHeaderTitle: String = title

      override def modalBodyContents()(implicit fsc: FSContext): NodeSeq = contents(this)(fsc)

      override def modalFooterContents()(implicit fsc: FSContext): Option[NodeSeq] = Some(modalFooterButton.lbl(closeBtnText).onclick(hideAndRemoveAndDeleteContext()).btn)
    }
    modal.installAndShow() & modal.onHidden(onHidden)
  }

  def okCancel(
                title: String,
                onOk: BSModal5Base => FSContext => Js,
                onCancel: BSModal5Base => FSContext => Js = modal => implicit fsc => modal.hideAndRemoveAndDeleteContext(),
                okBtnText: String = "OK",
                cancelBtnText: String = "Cancel",
                onHidden: Js = JS.void
              )(
                contents: BSModal5Base => FSContext => NodeSeq
              )(implicit fsc: FSContext): Js = {
    import com.fastscala.components.bootstrap5.helpers.BSHelpers.*
    val modal = new BSModal5Base {
      override def modalHeaderTitle: String = title

      override def modalBodyContents()(implicit fsc: FSContext): NodeSeq = contents(this)(fsc)

      override def modalFooterContents()(implicit fsc: FSContext): Option[NodeSeq] =
        Some(BSBtn().BtnSecondary.lbl(cancelBtnText).ajax(onCancel(this)).btn ++ BSBtn().BtnPrimary.lbl(okBtnText).ajax(onOk(this)).btn.ms_2)
    }
    modal.installAndShow() & modal.onHidden(onHidden)
  }
}
