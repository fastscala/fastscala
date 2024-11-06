package com.fastscala.demo.docs.forms

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form7.fields._
import com.fastscala.templates.form7.fields.layout.F7VerticalField
import com.fastscala.templates.form7.fields.text._
import com.fastscala.templates.form7.{DefaultForm7, F7Field}

import java.time.LocalDate

class FormValidationPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Form 7 Validation"

  import DefaultFSDemoBSForm7Renderers._
  import com.fastscala.templates.bootstrap5.helpers.BSHelpers._

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderSnippet("String input") {
      new DefaultForm7() {
        override lazy val rootField: F7Field = F7VerticalField()(

          new F7StringField().label("Name")
            .addValidation(valid_? = field => field.currentValue.length > 5, error = field => <span>Minimum length of 5 chars</span>),

          new F7DoubleField().label("Your height")
            .addValidation(field => field.currentValue > 100, field => <span>Minimum height 100cm</span>),

          new F7LocalDateOptField().label("Date")
            .addValidation(_.currentValue.exists(_.isAfter(LocalDate.now())), _ => <span>Must be a date in the future.</span>),

          new F7SubmitButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block.w_100)
        )
      }.render()
    }
    closeSnippet()
  }
}
