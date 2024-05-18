package com.fastscala.demo.docs.forms

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.modals.BSModal5
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form6.DefaultForm6
import com.fastscala.templates.form6.fields._

import java.time.format.DateTimeFormatter

class FormInputTypesPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Form 6 Input Types"

  import DefaultBSForm6Renderer._
  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

  override def renderExamples()(implicit fsc: FSContext): Unit = {
    renderSnippet("String input") {
      val inputField = new F6StringField().label("Name")

      div.border.p_2.rounded.apply {
        new DefaultForm6() {
          override def afterSave()(implicit fsc: FSContext): Js =
            BSModal5.verySimple("Your input", "Done")(modal => implicit fsc => fs_4.apply(s"Your name is ${inputField.currentValue}"))

          override lazy val rootField: FormField = F6VerticalField()(
            inputField
            , new F6SaveButtonField(implicit fsc => BSBtn.BtnPrimary.lbl("Submit").btn.d_block)
          )
        }.render()
      }
    }
    renderSnippet("(Optional) String input") {
      val inputField = new F6StringOptField().label("Name")

      div.border.p_2.rounded.apply {
        new DefaultForm6() {
          override def afterSave()(implicit fsc: FSContext): Js =
            BSModal5.verySimple("Your input", "Done")(modal => implicit fsc => fs_4.apply(s"Your name is ${inputField.currentValue.getOrElse("[None provided]")}"))

          override lazy val rootField: FormField = F6VerticalField()(
            inputField
            , new F6RawHtmlField(p.apply("(Experiment with submitting an empty input)"))
            , new F6SaveButtonField(implicit fsc => BSBtn.BtnPrimary.lbl("Submit").btn.d_block)
          )
        }.render()
      }
    }
    renderSnippet("(Optional) Double input") {
      val inputField = new F6DoubleOptField().label("Your height")

      div.border.p_2.rounded.apply {
        new DefaultForm6() {
          override def afterSave()(implicit fsc: FSContext): Js =
            BSModal5.verySimple("Your input", "Done")(modal => implicit fsc => fs_4.apply(s"Your input is: ${inputField.currentValue.getOrElse("[None provided]")}"))

          override lazy val rootField: FormField = F6VerticalField()(
            inputField
            , new F6RawHtmlField(p.apply("(Experiment with submitting an empty input)"))
            , new F6SaveButtonField(implicit fsc => BSBtn.BtnPrimary.lbl("Submit").btn.d_block)
          )
        }.render()
      }
    }
    renderSnippet("Double input") {
      val inputField = new F6DoubleField().label("Your height")

      div.border.p_2.rounded.apply {
        new DefaultForm6() {
          override def afterSave()(implicit fsc: FSContext): Js =
            BSModal5.verySimple("Your input", "Done")(modal => implicit fsc => fs_4.apply(s"Your input is: ${inputField.currentValue}"))

          override lazy val rootField: FormField = F6VerticalField()(
            inputField
            , new F6RawHtmlField(p.apply("(Experiment with submitting an empty input)"))
            , new F6SaveButtonField(implicit fsc => BSBtn.BtnPrimary.lbl("Submit").btn.d_block)
          )
        }.render()
      }
    }
    renderSnippet("(Optional) Int input") {
      val inputField = new F6IntOptField().label("Your age")

      div.border.p_2.rounded.apply {
        new DefaultForm6() {
          override def afterSave()(implicit fsc: FSContext): Js =
            BSModal5.verySimple("Your input", "Done")(modal => implicit fsc => fs_4.apply(s"Your input is: ${inputField.currentValue.getOrElse("[None provided]")}"))

          override lazy val rootField: FormField = F6VerticalField()(
            inputField
            , new F6RawHtmlField(p.apply("(Experiment with submitting an empty input)"))
            , new F6SaveButtonField(implicit fsc => BSBtn.BtnPrimary.lbl("Submit").btn.d_block)
          )
        }.render()
      }
    }
    renderSnippet("java.time.LocalDate input") {
      val inputField = new F6DateOptField().label("Date")

      div.border.p_2.rounded.apply {
        new DefaultForm6() {
          override def afterSave()(implicit fsc: FSContext): Js =
            BSModal5.verySimple("Your input", "Done")(modal => implicit fsc =>
              fs_4.apply(s"Selected date is ${inputField.currentValue.map(_.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))).getOrElse("[None selected]")}"))

          override lazy val rootField: FormField = F6VerticalField()(
            inputField
            , new F6SaveButtonField(implicit fsc => BSBtn.BtnPrimary.lbl("Submit").btn.d_block)
          )
        }.render()
      }
    }
    renderSnippet("java.time.LocalDateTime input") {
      val inputField = new F6DateTimeOptField().label("Date/time")

      div.border.p_2.rounded.apply {
        new DefaultForm6() {
          override def afterSave()(implicit fsc: FSContext): Js =
            BSModal5.verySimple("Your input", "Done")(modal => implicit fsc =>
              fs_4.apply(s"Selected date/time is ${inputField.currentValue.map(_.format(DateTimeFormatter.ofPattern("HH:mm dd MMM yyyy"))).getOrElse("[None selected]")}"))

          override lazy val rootField: FormField = F6VerticalField()(
            inputField
            , new F6SaveButtonField(implicit fsc => BSBtn.BtnPrimary.lbl("Submit").btn.d_block)
          )
        }.render()
      }
    }
    closeSnippet()
  }
}
