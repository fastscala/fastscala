package com.fastscala.demo.docs.forms

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.form7.layout.F7BSFormInputGroup
import com.fastscala.templates.bootstrap5.modals.BSModal5
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form7.fields._
import com.fastscala.templates.form7.fields.layout.F7VerticalField
import com.fastscala.templates.form7.fields.text._
import com.fastscala.templates.form7.{DefaultForm7, F7Field}

class FormInputGroupsPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Form 7 Input Groups"

  import DefaultFSDemoBSForm7Renderers._
  import com.fastscala.templates.bootstrap5.helpers.BSHelpers._

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {

    renderSnippet("Input groups") {

      val userhandle = new F7StringField().placeholder("Your user handle")
      val firstNameField = new F7StringOptField().help("On the left input your first/given name").isRequired
      val lastNameField = new F7StringOptField().help("To the right enter your last/family name").isRequired
      val commentsField = new F7StringTextareaField().help("Example with textarea")
      val amountField = new F7DoubleField().label("Monetary value")
      val heightField = new F7IntField().label("Your height")
      val enableOldHandleMigrationField = new F7CheckboxField().label("Migrate old user handle")
      val oldHandleField = new F7StringField().placeholder("Your old user handle").dependsOn(enableOldHandleMigrationField).disabled(() => !enableOldHandleMigrationField.currentValue)

      div.border.p_2.rounded.apply {
        new DefaultForm7() {

          validateBeforeUserInput()

          override lazy val rootField: F7Field = F7VerticalField()(
            new F7BSFormInputGroup()(new F7HtmlField(<label>@</label>), userhandle),
            new F7BSFormInputGroup()(enableOldHandleMigrationField, oldHandleField),
            new F7BSFormInputGroup()(commentsField, new F7HtmlField(<label>Your comments</label>)),
            new F7HtmlField(<h6>With help and validations</h6>),
            new F7BSFormInputGroup()(
              new F7HtmlField(<label>Full name:</label>),
              firstNameField,
              lastNameField,
            ),
            new F7BSFormInputGroup()(
              F7HtmlField.label("$"),
              amountField,
              F7HtmlField.label(".00"),
            ),
            new F7BSFormInputGroup()(heightField, F7HtmlField.label("meters")).inputGroupSizeLarge(),
            new F7SubmitButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block.w_100),
          )

          override def postSubmitForm()(implicit fsc: FSContext): Js =
            BSModal5.verySimple("Your input", "Done")(modal => implicit fsc =>
              fs_4.apply(s"Your data:") ++
                <ul>
                  <li><b>User Handle Field:</b> {userhandle.currentValue}</li>
                  <li><b>Enable Old User Handle Migration Field:</b> {enableOldHandleMigrationField.currentValue}</li>
                  <li><b>Old User Handle Field:</b> {oldHandleField.currentValue}</li>
                  <li><b>Comments Field:</b> {commentsField.currentValue}</li>
                  <li><b>First Name Field:</b> {firstNameField.currentValue}</li>
                  <li><b>Last Name Field:</b> {lastNameField.currentValue}</li>
                  <li><b>Amount Field:</b> {amountField.currentValue.formatted("%.2f")}</li>
                  <li><b>Height Field:</b> {heightField.currentValue}</li>
                </ul>
            )
        }.render()
      }
    }
    closeSnippet()
  }
}
