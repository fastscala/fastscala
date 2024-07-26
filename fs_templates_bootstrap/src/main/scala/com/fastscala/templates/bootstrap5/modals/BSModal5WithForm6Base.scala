package com.fastscala.templates.bootstrap5.modals

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form6.{F6FormRenderer, Form6}

abstract class BSModal5WithForm6Base[E <: FSXmlEnv : FSXmlSupport](
                                                                    val modalHeaderTitle: String
                                                                  )(implicit val formRenderer: F6FormRenderer[E]) extends BSModal5Base with Form6[E] {

  def saveBtnLbl = "Save"

  def saveBtn(implicit fsc: FSContext) = BSBtn().BtnPrimary.lbl(saveBtnLbl).onclick(form.onSaveClientSide())

  override def modalBodyContents()(implicit fsc: FSContext): E#NodeSeq = form.render()

  override def modalFooterContents()(implicit fsc: FSContext): Option[E#NodeSeq] = Some {
    BSBtn().BtnPrimary.lbl(saveBtnLbl).onclick(form.onSaveClientSide()).btn
  }
}
