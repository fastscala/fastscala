package com.fastscala.demo.examples.bootstrap

import com.fastscala.core.FSContext
import com.fastscala.demo.examples.MultipleCodeExamplesPage
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.utils.BSBtn

import scala.xml.NodeSeq

class BootstrapButtonsPage extends MultipleCodeExamplesPage("/com/fastscala/demo/examples/bootstrap/BootstrapButtonsPage.scala") {

  override def pageTitle: String = "Bootstrap examples"

  override def renderStandardPageContents()(implicit fsc: FSContext): NodeSeq = {

    import com.fastscala.templates.bootstrap5.classes.BSHelpers._

    codeSnippet(file, "=== code snippet #1 ===") ++ div.apply {
      // === code snippet #1 ===
      h6.apply("A button:") ++
        BSBtn.BtnPrimary.lbl("Example Button").onclick(Js.alert("clicked!")).btn
      // === code snippet #1 ===
    }.mb_3 ++ codeSnippet(file, "=== code snippet #2 ===") ++ div.apply {
      // === code snippet #2 ===
      h6.apply("Button styles:") ++
        BSBtn.BtnPrimary.lbl("Style Primary").sm.btn.ms_2.mb_2 ++
        BSBtn.BtnSecondary.lbl("Style Secondary").sm.btn.ms_2.mb_2 ++
        BSBtn.BtnInfo.lbl("Style Info").sm.btn.ms_2.mb_2 ++
        BSBtn.BtnDanger.lbl("Style Danger").sm.btn.ms_2.mb_2 ++
        BSBtn.BtnDark.lbl("Style Dark").sm.btn.ms_2.mb_2 ++
        BSBtn.BtnLight.lbl("Style Light").sm.btn.ms_2.mb_2 ++
        BSBtn.BtnSuccess.lbl("Style Success").sm.btn.ms_2.mb_2 ++
        BSBtn.BtnWarning.lbl("Style Warning").sm.btn.ms_2.mb_2 ++
        <br/> ++
        BSBtn.BtnOutlineDanger.lbl("Style Danger Outline").sm.btn.ms_2.mb_2 ++
        BSBtn.BtnOutlineDark.lbl("Style Dark Outline").sm.btn.ms_2.mb_2 ++
        BSBtn.BtnOutlineInfo.lbl("Style Info Outline").sm.btn.ms_2.mb_2 ++
        BSBtn.BtnOutlineLight.lbl("Style Light Outline").sm.btn.ms_2.mb_2 ++
        BSBtn.BtnOutlinePrimary.lbl("Style Primary Outline").sm.btn.ms_2.mb_2 ++
        BSBtn.BtnOutlineSecondary.lbl("Style Secondary Outline").sm.btn.ms_2.mb_2 ++
        BSBtn.BtnOutlineSuccess.lbl("Style Success Outline").sm.btn.ms_2.mb_2 ++
        BSBtn.BtnOutlineWarning.lbl("Style Warning Outline").sm.btn.ms_2.mb_2
      // === code snippet #2 ===
    }.mb_3 ++ codeSnippet(file, "=== code snippet #3 ===") ++ div.apply {
      // === code snippet #3 ===
      h6.apply("Button sizes:") ++
        BSBtn.BtnPrimary.lbl("Size lg").lg.btn.ms_2 ++
        BSBtn.BtnPrimary.lbl("Size normal").btn.ms_2 ++
        BSBtn.BtnPrimary.lbl("Size sm").sm.btn.ms_2
      // === code snippet #3 ===
    }.mb_3
  }
}
