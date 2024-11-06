package com.fastscala.templates.bootstrap5.modals

import com.fastscala.core.FSContext
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form7.{F7FormRenderer, Form7}
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils

import scala.xml.NodeSeq

abstract class BSModal5WithForm7Base(
                                      val modalHeaderTitle: String
                                    )(implicit val formRenderer: F7FormRenderer) extends BSModal5Base with Form7 {

  import com.fastscala.templates.bootstrap5.helpers.BSHelpers._

  def saveBtnLbl = "Save"

  def cancelBtnLbl = "Cancel"

  def cancelBtnEnabled: Boolean = false

  def saveBtn(implicit fsc: FSContext) = BSBtn().BtnPrimary.lbl(saveBtnLbl).ajax(implicit fsc => form.submitFormServerSide())

  def cancelBtn(implicit fsc: FSContext) = BSBtn().BtnSecondary.lbl(cancelBtnLbl).onclick(hideAndRemoveAndDeleteContext())

  override def modalBodyContents()(implicit fsc: FSContext): NodeSeq = form.render()

  override def modalFooterContents()(implicit fsc: FSContext): Option[NodeSeq] = Some {
    ScalaXmlElemUtils.showIf(cancelBtnEnabled)(cancelBtn.btn.me_2) ++ saveBtn.btn
  }
}
