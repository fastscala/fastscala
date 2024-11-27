package com.fastscala.demo.docs.forms

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.bootstrap5.modals.BSModal5
import com.fastscala.components.bootstrap5.utils.BSBtn
import com.fastscala.components.form7.fields.*
import com.fastscala.components.form7.fields.layout.F7VerticalField
import com.fastscala.components.form7.{DefaultForm7, F7Field}
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

class FormInputTypesPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Form 7 Input Types"

  import DefaultFSDemoBSForm7Renderers.*
  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

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
