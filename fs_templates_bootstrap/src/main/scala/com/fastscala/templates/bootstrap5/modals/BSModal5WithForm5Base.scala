package com.fastscala.templates.bootstrap5.modals

import com.fastscala.core.FSContext
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form5.{Form5, FormRenderer}

import scala.xml.NodeSeq

abstract class BSModal5WithForm5Base(
                                     val modalHeaderTitle: String
                                   )(implicit val formRenderer: FormRenderer) extends BSModal5Base with Form5 {

  def saveBtnLbl = "Save"

  override def modalBodyContents()(implicit fsc: FSContext): NodeSeq = form.render()

  override def modalFooterContents()(implicit fsc: FSContext): Option[NodeSeq] = Some {
    BSBtn.BtnPrimary.lbl(saveBtnLbl).onclick(form.onSaveClientSide()).btn
  }
}
