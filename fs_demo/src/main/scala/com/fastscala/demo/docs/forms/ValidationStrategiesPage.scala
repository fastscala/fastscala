package com.fastscala.demo.docs.forms

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.components.bootstrap5.utils.BSBtn
import com.fastscala.components.form7.fields.*
import com.fastscala.components.form7.fields.layout.F7VerticalField
import com.fastscala.components.form7.fields.text.*
import com.fastscala.components.form7.{DefaultForm7, F7Field}

class ValidationStrategiesPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Form 7 Validation Strategies"

  import DefaultFSDemoBSForm7Renderers.*
  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderSnippet("ValidateBeforeUserInput (always validates)") {
      new DefaultForm7() {
        validateBeforeUserInput()
        override lazy val rootField: F7Field = F7VerticalField()(
          new F7StringField().label("Name")
            .addValidation(_.currentValue.length >= 5, _ => <span>Error: input less than 5 characters</span>)
            .help("Min. 5 chars")
          , new F7IntOptField().label("Integer field")
          , new F7SubmitButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block.w_100)
        )
      }.render()
    }
    renderSnippet("ValidateEachFieldAfterUserInput (validates immediately after input in a field)") {
      new DefaultForm7() {
        validateEachFieldAfterUserInput()
        override lazy val rootField: F7Field = F7VerticalField()(
          new F7StringField().label("Name (type abc and move to next field)")
            .addValidation(_.currentValue.length >= 5, _ => <span>Error: input less than 5 characters</span>)
            .help("Min. 5 chars")
          , new F7IntOptField().label("Integer field")
          , new F7SubmitButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block.w_100)
        )
      }.render()
    }
    renderSnippet("ValidateOnAttemptSubmitOnly (only validates on submit)") {
      new DefaultForm7() {
        validateOnAttemptSubmitOnly()
        override lazy val rootField: F7Field = F7VerticalField()(
          new F7StringField().label("Name")
            .addValidation(_.currentValue.length >= 5, _ => <span>Error: input less than 5 characters</span>)
            .help("Min. 5 chars")
          , new F7IntOptField().label("Integer field")
          , new F7SubmitButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block.w_100)
        )
      }.render()
    }
    closeSnippet()
  }
}
