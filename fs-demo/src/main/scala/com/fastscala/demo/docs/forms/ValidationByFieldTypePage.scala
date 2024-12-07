package com.fastscala.demo.docs.forms

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.bootstrap5.form7.renderermodifiers.CheckboxStyle
import com.fastscala.components.bootstrap5.modals.BSModal5
import com.fastscala.components.bootstrap5.toast.BSToast2
import com.fastscala.components.bootstrap5.utils.BSBtn
import com.fastscala.components.form7.fields.*
import com.fastscala.components.form7.fields.layout.F7VerticalField
import com.fastscala.components.form7.fields.multiselect.F7MultiSelectField
import com.fastscala.components.form7.fields.radio.F7RadioField
import com.fastscala.components.form7.fields.select.F7SelectField
import com.fastscala.components.form7.fields.text.*
import com.fastscala.components.form7.{DefaultForm7, F7Field}
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import java.awt.Color
import scala.util.chaining.scalaUtilChainingOps

class ValidationByFieldTypePage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Form 7 Input Types"

  import DefaultFSDemoBSForm7Renderers.*
  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderSnippet("String input") {
      new DefaultForm7() {
        validateBeforeUserInput()

        override lazy val rootField: F7Field = F7VerticalField()(
          new F7StringField().label("Name").addValidation(_.currentValue.length > 5, _ => div.apply("Error: minimum of 5 chars"))
            .help("Input at least 5 characters")
          , new F7SubmitButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block)
        )

        override def postSubmitForm()(implicit fsc: FSContext): Js =
          BSToast2.VerySimple(div.apply(strong.apply("Submitted!").me_auto))(div.my_2.apply("Submission was successful")).installAndShow()
      }.render().pipe(renderedForm => {
        div.border.p_2.rounded.apply(renderedForm)
      })
    }
    renderSnippet("Textarea") {
      new DefaultForm7() {
        validateBeforeUserInput()

        override lazy val rootField: F7Field = F7VerticalField()(
          new F7StringTextareaField().label("Your message").addValidation(_.currentValue.split(" ").length > 5, _ => div.apply("Error: minimum of 5 words"))
            .help("Input at least 5 words")
          , new F7SubmitButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block)
        )

        override def postSubmitForm()(implicit fsc: FSContext): Js =
          BSToast2.VerySimple(div.apply(strong.apply("Submitted!").me_auto))(div.my_2.apply("Submission was successful")).installAndShow()
      }.render().pipe(renderedForm => {
        div.border.p_2.rounded.apply(renderedForm)
      })
    }
    renderSnippet("Select") {
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
      val inputField = new F7SelectField[(String, Color)](colors).label("Color").option2String(_._1)
        .addValidation(_.currentValue != colors.head, _ => div.apply("Error: cannot be white!"))
        .help("Choose a color different from white.")

      div.border.p_2.rounded.apply {
        new DefaultForm7() {
          validateBeforeUserInput()

          override def postSubmitForm()(implicit fsc: FSContext): Js =
            BSToast2.VerySimple(div.apply(strong.apply("Submitted!").me_auto))(div.my_2.apply("Submission was successful")).installAndShow()

          override lazy val rootField: F7Field = F7VerticalField()(
            inputField
            , new F7SubmitButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block)
          )
        }.render()
      }
    }
    renderSnippet("Multi Select") {
      val continents: List[String] = List(
        "Asia"
        , "Africa"
        , "North America"
        , "South America"
        , "Antarctica"
        , "Europe"
        , "Australia"
      )
      val inputField = new F7MultiSelectField().options(continents).label("Continents").size(10)
        .addValidation(_.currentValue.size >= 2, _ => div.apply("Error: please select at least 2 continents."))
        .help("Include at least two continents in your selection.")

      div.border.p_2.rounded.apply {
        new DefaultForm7() {
          validateBeforeUserInput()

          override def postSubmitForm()(implicit fsc: FSContext): Js = BSModal5.verySimple("Your input", "Done")(
            _ => implicit fsc => div.apply(s"Your selected continents: " + inputField.currentValue.mkString(", "))
          )

          override lazy val rootField: F7Field = F7VerticalField()(
            inputField
            , new F7SubmitButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block)
          )
        }.render()
      }
    }
    renderSnippet("Checkbox") {

      val inputField = new F7CheckboxField().label("I agree to the Terms of Service")
        .addValidation(_.currentValue == true, _ => div.apply("Error: you must accept the Terms of Service."))

      div.border.p_2.rounded.apply {
        new DefaultForm7() {
          validateBeforeUserInput()

          override def postSubmitForm()(implicit fsc: FSContext): Js = BSModal5.verySimple("Your input", "Done")(
            _ => implicit fsc => div.apply(s"Your selection: " + inputField.currentValue)
          )

          override lazy val rootField: F7Field = F7VerticalField()(
            inputField
            , new F7SubmitButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block)
          )
        }.render()
      }
    }
    renderSnippet("Checkbox as switch") {

      val renderers = new FSDemoBSForm7Renderers()(checkboxStyle = CheckboxStyle.Switch)
      import renderers.*

      // Pass the renderer which renders as switches:
      val inputField = new F7CheckboxField().label("I agree to the Terms of Service")
        .addValidation(_.currentValue == true, _ => div.apply("Error: you must accept the Terms of Service."))

      div.border.p_2.rounded.apply {
        new DefaultForm7() {
          validateBeforeUserInput()

          override def postSubmitForm()(implicit fsc: FSContext): Js = BSModal5.verySimple("Your input", "Done")(
            _ => implicit fsc => div.apply(s"Your selection: " + inputField.currentValue)
          )

          override lazy val rootField: F7Field = F7VerticalField()(
            inputField
            , new F7SubmitButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block)
          )
        }.render()
      }
    }
    renderSnippet("Radio") {

      val inputField = new F7RadioField[String](() => Seq("Android", "iOS", "Others")).label("Your phone")
        .setInternalValue("Others")
        .help("Select your phone type, iOS or Android (others not supported)")
        .addValidation(_.currentValue != "Others", _ => <div>Only Android and iOS are supported.</div>)

      div.border.p_2.rounded.apply {
        new DefaultForm7() {
          validateBeforeUserInput()

          override def postSubmitForm()(implicit fsc: FSContext): Js =
            BSModal5.verySimple("Your input", "Done")(modal => implicit fsc =>
              div.apply(s"Your phone type: ${inputField.currentValue}")
            )

          override lazy val rootField: F7Field = F7VerticalField()(
            inputField
            , new F7SubmitButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block)
          )
        }.render()
      }
    }
    closeSnippet()
    //    renderSnippet("Enum-based") {
    //
    //      object OutputState extends Enumeration {
    //        val High, Low, HighZ = Value
    //      }
    //
    //      val inputField = F7EnumField.Nullable(OutputState).label("Output State").option2String(_.map(_.toString).getOrElse("--"))
    //
    //      div.border.p_2.rounded.apply {
    //        new DefaultForm7() {
    //          validateBeforeUserInput()
    //
    //          override def postSubmitForm()(implicit fsc: FSContext): Js =
    //            BSModal5.verySimple("Your input", "Done")(modal => implicit fsc =>
    //              fs_4.apply(s"Your selection:") ++
    //                pre.apply(inputField.currentValue.toString)
    //            )
    //
    //          override lazy val rootField: F7Field = F7VerticalField()(
    //            inputField
    //            , new F7SaveButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block)
    //          )
    //        }.render()
    //      }
    //    }
    //    closeSnippet()
  }
}
