package com.fastscala.templates.bootstrap5.modals

import com.fastscala.core.FSContext
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form6.{Form6, F6FormRenderer}

import scala.xml.NodeSeq

abstract class BSModal5WithForm6Base(
                                      val modalHeaderTitle: String
                                    )(implicit val formRenderer: F6FormRenderer) extends BSModal5Base with Form6 {

  def saveBtnLbl = "Save"

  def saveBtn(implicit fsc: FSContext) = BSBtn.BtnPrimary.lbl(saveBtnLbl).onclick(form.onSaveClientSide())

  override def modalBodyContents()(implicit fsc: FSContext): NodeSeq = form.render()

  override def modalFooterContents()(implicit fsc: FSContext): Option[NodeSeq] = Some {
    BSBtn.BtnPrimary.lbl(saveBtnLbl).onclick(form.onSaveClientSide()).btn
  }
}
