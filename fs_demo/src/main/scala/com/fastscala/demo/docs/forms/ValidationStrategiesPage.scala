package com.fastscala.demo.docs.forms

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form7.fields._
import com.fastscala.templates.form7.fields.layout.F7VerticalField
import com.fastscala.templates.form7.fields.text._
import com.fastscala.templates.form7.{DefaultForm7, F7Field}

class ValidationStrategiesPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Form 7 Validation Strategies"

  import DefaultBSForm7Renderer._
  import com.fastscala.templates.bootstrap5.helpers.BSHelpers._

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderSnippet("ValidateBeforeUserInput") {
      new DefaultForm7() {
        validateBeforeUserInput()
        override lazy val rootField: F7Field = F7VerticalField()(
          new F7StringField().label("Name")
            .addValidation(_.currentValue.length > 5, _ => <span>Minimum length of 5 chars</span>)
          , new F7IntOptField().label("Integer field")
          , new F7SaveButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block.w_100)
        )
      }.render()
    }
    renderSnippet("ValidateEachFieldAfterUserInput") {
      new DefaultForm7() {
        validateEachFieldAfterUserInput()
        override lazy val rootField: F7Field = F7VerticalField()(
          new F7StringField().label("Name")
            .addValidation(_.currentValue.length > 5, _ => <span>Minimum length of 5 chars</span>)
          , new F7IntOptField().label("Integer field")
          , new F7SaveButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block.w_100)
        )
      }.render()
    }
    renderSnippet("ValidateOnAttemptSubmitOnly") {
      new DefaultForm7() {
        validateOnAttemptSubmitOnly()
        override lazy val rootField: F7Field = F7VerticalField()(
          new F7StringField().label("Name")
            .addValidation(_.currentValue.length > 5, _ => <span>Minimum length of 5 chars</span>)
          , new F7IntOptField().label("Integer field")
          , new F7SaveButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block.w_100)
        )
      }.render()
    }
    closeSnippet()
  }
}
