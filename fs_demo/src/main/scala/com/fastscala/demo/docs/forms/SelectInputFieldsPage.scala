package com.fastscala.demo.docs.forms

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.templates.bootstrap5.modals.BSModal5
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form7.fields.*
import com.fastscala.templates.form7.fields.layout.F7VerticalField
import com.fastscala.templates.form7.fields.multiselect.F7MultiSelectField
import com.fastscala.templates.form7.fields.select.{F7SelectField, F7SelectOptField}
import com.fastscala.templates.form7.{DefaultForm7, F7Field}
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import java.awt.Color

class SelectInputFieldsPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Form 7 Select Input Fields"

  import DefaultFSDemoBSForm7Renderers.*
  import com.fastscala.templates.bootstrap5.helpers.BSHelpers.*

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderSnippet("Select input-based fields") {

      val colors: List[(String, Color)] = List(
        "WHITE" -> java.awt.Color.WHITE
        , "LIGHT_GRAY" -> java.awt.Color.LIGHT_GRAY
        , "GRAY" -> java.awt.Color.GRAY
        , "DARK_GRAY" -> java.awt.Color.DARK_GRAY
        , "BLACK" -> java.awt.Color.BLACK
        , "RED" -> java.awt.Color.RED
        , "PINK" -> java.awt.Color.PINK
        , "ORANGE" -> java.awt.Color.ORANGE
        , "YELLOW" -> java.awt.Color.YELLOW
        , "GREEN" -> java.awt.Color.GREEN
        , "MAGENTA" -> java.awt.Color.MAGENTA
        , "CYAN" -> java.awt.Color.CYAN
        , "BLUE" -> java.awt.Color.BLUE
      )
      val selectField = new F7SelectField[(String, Color)](colors).label("Color").option2String(_._1)
        .help("One must always be selected")
      val selectOptField = new F7SelectOptField[(String, Color)]().optionsNonEmpty(colors).label("Color").option2String(_.map(_._1).getOrElse("--"))
        .help("Selection may be empty")
      val multiSelectField = new F7MultiSelectField().options(colors).label("Colors").size(8).option2String(_._1)
        .help("Multiple can be selected")

      div.apply {
        new DefaultForm7() {
          override lazy val rootField: F7Field = F7VerticalField()(
            selectField,
            selectOptField,
            multiSelectField,
            new F7SubmitButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block.w_100)
          )

          override def postSubmitForm()(implicit fsc: FSContext): Js = BSModal5.verySimple("Submitted!", "Ok") {
            modal =>
              implicit fsc =>
                <h6>Your data:</h6> ++
                <ul>
                  <li><b>Select Field:</b> {selectField.getInternalValue()._1}</li>
                  <li><b>Optional Select Field:</b> {selectOptField.getInternalValue().map(_._1)}</li>
                  <li><b>Multi-Select Field:</b> {multiSelectField.getInternalValue().map(_._1).mkString("; ")}</li>
                </ul>
          }

        }.render()
      }
    }
    closeSnippet()
  }
}
