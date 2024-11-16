package com.fastscala.demo.docs.forms

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.templates.bootstrap5.form7.renderermodifiers.{CheckboxAlignment, CheckboxSide, CheckboxStyle}
import com.fastscala.templates.bootstrap5.modals.BSModal5
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form7.fields.*
import com.fastscala.templates.form7.fields.layout.F7VerticalField
import com.fastscala.templates.form7.fields.radio.F7RadioField
import com.fastscala.templates.form7.{DefaultForm7, F7Field}
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

class RadioInputFieldsPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Form 7 Radio Input Fields"

  import DefaultFSDemoBSForm7Renderers.*
  import com.fastscala.templates.bootstrap5.helpers.BSHelpers.*

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderSnippet("Radio input-based fields") {

      val radioAsSwitchAndOpposite = new FSDemoBSForm7Renderers()(checkboxStyle = CheckboxStyle.Switch, checkboxSide = CheckboxSide.Opposite).radioFieldRenderer
      val radioInline = new FSDemoBSForm7Renderers()(checkboxAlignment = CheckboxAlignment.Horizontal).radioFieldRenderer

      val registrationType = Seq("Teacher", "Student", "Parent")
      val registrationTypeField = new F7RadioField[String](() => registrationType).label("Registration type")

      val phoneType = Seq("Android", "iOS", "Other")
      val phoneTypeField = new F7RadioField[String](() => phoneType)(radioAsSwitchAndOpposite).label("Phone type")

      val marketingChannelsType = Seq("Android", "iOS", "Other")
      val marketingChannelsTypeField = new F7RadioField[String](() => marketingChannelsType)(radioInline).label("Preferred marketing channel")

      div.apply {
        new DefaultForm7() {
          override lazy val rootField: F7Field = F7VerticalField()(
            new F7HtmlField(fs_5.mb_3.border_bottom.apply("Radio input")),
            registrationTypeField,
            //
            new F7HtmlField(fs_5.mb_3.border_bottom.apply("Radio buttons as switches on the opposite side")),
            phoneTypeField,
            //
            new F7HtmlField(fs_5.mb_3.border_bottom.apply("Radio buttons inline")),
            marketingChannelsTypeField,
            new F7SubmitButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block.w_100)
          )

          override def postSubmitForm()(implicit fsc: FSContext): Js = BSModal5.verySimple("Submitted!", "Ok") {
            modal =>
              implicit fsc =>
                <h6>Your data:</h6> ++
                <ul>
                  <li><b>Registration type:</b> {registrationTypeField.getInternalValue()}</li>
                  <li><b>Phone type:</b> {phoneTypeField.getInternalValue()}</li>
                  <li><b>Preferred marketing channel:</b> {marketingChannelsTypeField.getInternalValue()}</li>
                </ul>
          }

        }.render()
      }
    }
    closeSnippet()
  }
}
