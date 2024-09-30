package com.fastscala.demo.docs.bootstrap

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.utils.BSBtn

class BootstrapButtonsPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Bootstrap Buttons"

  import com.fastscala.templates.bootstrap5.helpers.BSHelpers._

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {

    renderSnippet("Simple Button") {
      div.withContents {
        BSBtn().BtnPrimary.lbl("Example Button").onclick(Js.alert("clicked!")).btn
      }
    }
    renderSnippet("Button styles") {
      div.withContents {
        (BSBtn().BtnPrimary.lbl("Style Primary").sm.btn.ms_2.mb_2 ++
          BSBtn().BtnSecondary.lbl("Style Secondary").sm.btn.ms_2.mb_2 ++
          BSBtn().BtnInfo.lbl("Style Info").sm.btn.ms_2.mb_2 ++
          BSBtn().BtnDanger.lbl("Style Danger").sm.btn.ms_2.mb_2 ++
          BSBtn().BtnDark.lbl("Style Dark").sm.btn.ms_2.mb_2 ++
          BSBtn().BtnLight.lbl("Style Light").sm.btn.ms_2.mb_2 ++
          BSBtn().BtnSuccess.lbl("Style Success").sm.btn.ms_2.mb_2 ++
          BSBtn().BtnWarning.lbl("Style Warning").sm.btn.ms_2.mb_2 ++
          <br/> ++
          BSBtn().BtnOutlineDanger.lbl("Style Danger Outline").sm.btn.ms_2.mb_2 ++
          BSBtn().BtnOutlineDark.lbl("Style Dark Outline").sm.btn.ms_2.mb_2 ++
          BSBtn().BtnOutlineInfo.lbl("Style Info Outline").sm.btn.ms_2.mb_2 ++
          BSBtn().BtnOutlineLight.lbl("Style Light Outline").sm.btn.ms_2.mb_2 ++
          BSBtn().BtnOutlinePrimary.lbl("Style Primary Outline").sm.btn.ms_2.mb_2 ++
          BSBtn().BtnOutlineSecondary.lbl("Style Secondary Outline").sm.btn.ms_2.mb_2 ++
          BSBtn().BtnOutlineSuccess.lbl("Style Success Outline").sm.btn.ms_2.mb_2 ++
          BSBtn().BtnOutlineWarning.lbl("Style Warning Outline").sm.btn.ms_2.mb_2)
      }
    }
    renderSnippet("Button sizes") {
      BSBtn().BtnPrimary.lbl("Size lg").lg.btn.ms_2 ++
        BSBtn().BtnPrimary.lbl("Size normal").btn.ms_2 ++
        BSBtn().BtnPrimary.lbl("Size sm").sm.btn.ms_2
    }
    closeSnippet()
  }
}
