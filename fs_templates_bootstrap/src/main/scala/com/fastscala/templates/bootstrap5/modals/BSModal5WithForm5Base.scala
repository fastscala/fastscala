package com.fastscala.templates.bootstrap5.modals

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form5.{Form5, FormRenderer}

abstract class BSModal5WithForm5Base[E <: FSXmlEnv : FSXmlSupport](
                                                                    val modalHeaderTitle: String
                                                                  )(implicit val formRenderer: FormRenderer[E]) extends BSModal5Base with Form5[E] {

  def saveBtnLbl = "Save"

  override def modalBodyContents()(implicit fsc: FSContext): E#NodeSeq = form.render()

  override def modalFooterContents()(implicit fsc: FSContext): Option[E#NodeSeq] = Some {
    BSBtn().BtnPrimary.lbl(saveBtnLbl).onclick(form.onSaveClientSide()).btn
  }
}
