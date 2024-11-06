package com.fastscala.demo.docs.forms

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.form7.renderermodifiers.{CheckboxAlignment, CheckboxSide, CheckboxStyle}
import com.fastscala.templates.bootstrap5.modals.BSModal5
import com.fastscala.templates.bootstrap5.toast.BSToast2
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form7.fields._
import com.fastscala.templates.form7.fields.layout.F7VerticalField
import com.fastscala.templates.form7.fields.multiselect.F7MultiSelectField
import com.fastscala.templates.form7.fields.radio.F7RadioField
import com.fastscala.templates.form7.fields.select.{F7SelectField, F7SelectOptField}
import com.fastscala.templates.form7.fields.text._
import com.fastscala.templates.form7.{DefaultForm7, F7Field}

import java.awt.Color
import java.time.format.DateTimeFormatter

class FormInputTypesPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Form 7 Input Types"

  import DefaultFSDemoBSForm7Renderers._
  import com.fastscala.templates.bootstrap5.helpers.BSHelpers._

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {

    renderSnippet("Enum-based") {

      object OutputState extends Enumeration {
        val High, Low, HighZ = Value
      }

      val inputField = F7EnumField.Nullable(OutputState).label("Output State").option2String(_.map(_.toString).getOrElse("--"))

      div.border.p_2.rounded.apply {
        new DefaultForm7() {
          override def postSubmitForm()(implicit fsc: FSContext): Js =
            BSModal5.verySimple("Your input", "Done")(modal => implicit fsc =>
              fs_4.apply(s"Your selection:") ++
                pre.apply(inputField.currentValue.toString)
            )

          override lazy val rootField: F7Field = F7VerticalField()(
            inputField
            , new F7SubmitButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block)
          )
        }.render()
      }
    }
    closeSnippet()
  }
}
