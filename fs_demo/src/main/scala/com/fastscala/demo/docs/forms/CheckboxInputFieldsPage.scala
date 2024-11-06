package com.fastscala.demo.docs.forms

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.form7.renderermodifiers.{CheckboxAlignment, CheckboxSide, CheckboxStyle}
import com.fastscala.templates.bootstrap5.modals.BSModal5
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form7.fields._
import com.fastscala.templates.form7.fields.layout.{F7ContainerField, F7VerticalField}
import com.fastscala.templates.form7.{DefaultForm7, F7Field}

class CheckboxInputFieldsPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Form 7 Checkbox Input Fields"

  import DefaultFSDemoBSForm7Renderers._
  import com.fastscala.templates.bootstrap5.helpers.BSHelpers._

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderSnippet("Checkbox input-based fields") {

      val checkboxAsSwitchAndOpposite = new FSDemoBSForm7Renderers()(checkboxStyle = CheckboxStyle.Switch, checkboxSide = CheckboxSide.Opposite).checkboxFieldRenderer
      val checkboxInline = new FSDemoBSForm7Renderers()(checkboxAlignment = CheckboxAlignment.Horizontal).checkboxFieldRenderer

      val termsAndConditionsField = new F7CheckboxField().label("Accept Terms and Conditions")
      val privacyPolicyField = new F7CheckboxField().label("Accept Privacy Policy").setInternalValue(true)

      val hasACar = new F7CheckboxOptField().label("Has car").setInternalValue(None)

      val subscribe2NewsletterField = new F7CheckboxField()(checkboxInline).label("Subscribe to Newsletter")
      val subscribe2DiscountsField = new F7CheckboxField()(checkboxInline).label("Subscribe to Discounts").setInternalValue(true)

      val marketingEmailField = new F7CheckboxField()(checkboxAsSwitchAndOpposite).label("Email")
      val marketingSMSField = new F7CheckboxField()(checkboxAsSwitchAndOpposite).label("SMS").setInternalValue(true)
      val marketingPhoneField = new F7CheckboxField()(checkboxAsSwitchAndOpposite).label("Phone")

      div.apply {
        new DefaultForm7() {
          override lazy val rootField: F7Field = F7VerticalField()(
            new F7HtmlField(fs_5.mb_3.border_bottom.apply("Checkboxes")),
            termsAndConditionsField,
            privacyPolicyField,
            //
            new F7HtmlField(fs_5.mb_3.border_bottom.apply("Tri-state checkboxes (supports indeterminate)")),
            new F7ContainerField("row")(
              "col" -> hasACar,
              "col" -> new F7HtmlField(span.apply("Checkbox state: " + hasACar.currentValue.map("set to " + _).getOrElse("not set"))).dependsOn(hasACar),
            ),
            //
            new F7HtmlField(fs_5.mb_3.border_bottom.apply("Checkboxes inline")),
            subscribe2NewsletterField,
            subscribe2DiscountsField,
            //
            new F7HtmlField(fs_5.mb_3.border_bottom.apply("Checkboxes as switches and opposite side")),
            marketingEmailField,
            marketingSMSField,
            marketingPhoneField,
            new F7SubmitButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block.w_100)
          )

          override def postSubmitForm()(implicit fsc: FSContext): Js = BSModal5.verySimple("Submitted!", "Ok") {
            modal =>
              implicit fsc =>
                <h6>Your data:</h6> ++
                <ul>
                  <li><b>Accept Terms and Conditions:</b> {termsAndConditionsField.getInternalValue()}</li>
                  <li><b>Accept Privacy Policy:</b> {privacyPolicyField.getInternalValue()}</li>
                  <li><b>Has car:</b> {hasACar.getInternalValue()}</li>
                  <li><b>Subscribe to Newsletter:</b> {subscribe2NewsletterField.getInternalValue()}</li>
                  <li><b>Subscribe to Discounts:</b> {subscribe2DiscountsField.getInternalValue()}</li>
                  <li><b>Email:</b> {marketingEmailField.getInternalValue()}</li>
                  <li><b>SMS:</b> {marketingSMSField.getInternalValue()}</li>
                  <li><b>Phone:</b> {marketingPhoneField.getInternalValue()}</li>
                </ul>
          }

        }.render()
      }
    }
    closeSnippet()
  }
}
