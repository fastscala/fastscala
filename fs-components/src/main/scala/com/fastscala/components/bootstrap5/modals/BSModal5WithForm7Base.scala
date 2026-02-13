package com.fastscala.components.bootstrap5.modals

import com.fastscala.components.bootstrap5.utils.BSBtn
import com.fastscala.components.form7.Form7
import com.fastscala.components.form7.renderers.F7FormRenderer
import com.fastscala.core.FSContext
import com.fastscala.scala_xml.ScalaXmlElemUtils

import scala.xml.NodeSeq

abstract class BSModal5WithForm7Base(
                                      val modalHeaderTitle: String
                                    )(implicit val formRenderer: F7FormRenderer) extends BSModal5Base with Form7 {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  def saveBtnLbl = "Save"

  def cancelBtnLbl = "Cancel"

  def cancelBtnEnabled: Boolean = false

  def saveBtn(implicit fsc: FSContext) = BSBtn().BtnPrimary.lbl(saveBtnLbl).callback(implicit fsc => form.submitFormServerSide())

  def cancelBtn(implicit fsc: FSContext) = BSBtn().BtnSecondary.lbl(cancelBtnLbl).onclick(hideAndRemoveAndDeleteContext())

  override def modalBodyContents()(implicit fsc: FSContext): NodeSeq = form.render()

  override def modalFooterContents()(implicit fsc: FSContext): Option[NodeSeq] = Some {
    ScalaXmlElemUtils.showIf(cancelBtnEnabled)(cancelBtn.btn.me_2) ++ saveBtn.btn
  }
}
