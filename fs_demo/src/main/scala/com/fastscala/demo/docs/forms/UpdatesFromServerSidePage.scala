package com.fastscala.demo.docs.forms

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.demo.docs.data.{Continents, Fruit}
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.form7.layout.F7BSFormInputGroup
import com.fastscala.templates.bootstrap5.toast.BSToast2
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.templates.form7.fields._
import com.fastscala.templates.form7.fields.layout.{F7ContainerField, F7VerticalField}
import com.fastscala.templates.form7.fields.multiselect.F7MultiSelectField
import com.fastscala.templates.form7.fields.radio.F7RadioField
import com.fastscala.templates.form7.fields.select.F7SelectField
import com.fastscala.templates.form7.fields.text._
import com.fastscala.templates.form7.{DefaultForm7, F7Field}

import scala.util.Random

class UpdatesFromServerSidePage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Form 7 - Update client values after changes on server side"

  import DefaultFSDemoBSForm7Renderers._
  import com.fastscala.templates.bootstrap5.helpers.BSHelpers._

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {

    renderSnippet("Example") {

      val randomSeed: F7IntField = new F7IntField().step(1).label("Random seed").doSyncToServerOnChange

      val textField = new F7StringField().label("Text field example").dependsOn(randomSeed)
      val textFieldInInputGroup = new F7StringField().label("Text field in input group example").dependsOn(randomSeed)
      val textFieldInputGroup = new F7BSFormInputGroup()(F7HtmlField.label("Field:"), textFieldInInputGroup)
      val textareaField = new F7StringTextareaField().label("Textarea field example").dependsOn(randomSeed)
      val textareaFieldInInputGroup = new F7StringTextareaField().label("Textarea field in input group example").dependsOn(randomSeed)
      val textareaFieldInputGroup = new F7BSFormInputGroup()(F7HtmlField.label("Field:"), textareaFieldInInputGroup)
      val checkboxField = new F7CheckboxField().label("Checkbox field example").dependsOn(randomSeed)
      val checkboxFieldInInputGroup = new F7CheckboxField().label("Checkbox field in input group example").dependsOn(randomSeed)
      val checkboxFieldInputGroup = new F7BSFormInputGroup()(F7HtmlField.label("Field:"), checkboxFieldInInputGroup)
      val selectField = new F7SelectField[String](() => Fruit()).label("Select field example").dependsOn(randomSeed)
      val selectFieldInInputGroup = new F7SelectField[String](() => Fruit()).label("Select field in input group example").dependsOn(randomSeed)
      val selectFieldInputGroup = new F7BSFormInputGroup()(F7HtmlField.label("Field:"), selectFieldInInputGroup)
      val multiSelectField = new F7MultiSelectField[String]().options(Continents()).label("Multi-select field example").dependsOn(randomSeed)
      val multiSelectFieldInInputGroup = new F7MultiSelectField[String]().options(Continents()).label("Multi-select field in input group example").dependsOn(randomSeed)
      val multiSelectFieldInputGroup = new F7BSFormInputGroup()(F7HtmlField.label("Field:"), multiSelectFieldInInputGroup)
      val radioField = new F7RadioField[String](() => Continents()).label("Radio field example").dependsOn(randomSeed)

      def setValues(seed: Int): Unit = {
        val rand = new Random(randomSeed.currentValue)
        textField.currentValue = (1 to 8).map(_ => rand.nextPrintableChar()).mkString
        textFieldInInputGroup.currentValue = textField.currentValue
        textareaField.currentValue = (1 to 5).map(_ => (1 to 6).map(_ => rand.nextPrintableChar()).mkString).mkString(" ")
        textareaFieldInInputGroup.currentValue = textareaField.currentValue
        checkboxField.currentValue = rand.nextBoolean()
        checkboxFieldInInputGroup.currentValue = checkboxField.currentValue
        selectField.currentValue = Fruit()(rand.nextInt(Fruit().size))
        selectFieldInInputGroup.currentValue = selectField.currentValue
        multiSelectField.currentValue = Continents().filter(_ => rand.nextBoolean()).toSet
        multiSelectFieldInInputGroup.currentValue = multiSelectField.currentValue
        radioField.currentValue = Continents()(rand.nextInt(Continents().size))
      }

      randomSeed.addOnThisFieldChanged(f => {
        setValues(f.currentValue)
        Js.void
      })

      div.border.p_2.rounded.apply {
        new DefaultForm7() {

          validateBeforeUserInput()

          setValues(randomSeed.currentValue)

          override lazy val rootField: F7Field = F7VerticalField()(
            randomSeed,
            new F7ContainerField("row")("col" -> textField, "col" -> textFieldInputGroup),
            new F7ContainerField("row")("col" -> checkboxField, "col" -> checkboxFieldInputGroup),
            new F7ContainerField("row")("col" -> selectField, "col" -> selectFieldInputGroup),
            new F7ContainerField("row")("col" -> textareaField, "col" -> textareaFieldInputGroup),
            new F7ContainerField("row")("col" -> multiSelectField, "col" -> multiSelectFieldInputGroup),
            new F7ContainerField("row")("col" -> radioField),
            new F7SubmitButtonField(implicit fsc => BSBtn().BtnPrimary.lbl("Submit").btn.d_block.w_100),
          )

          override def postSubmitForm()(implicit fsc: FSContext): Js =
            BSToast2.VerySimple(label.apply("Submitted"))(div.my_1.apply("You submitted the form")).installAndShow()
        }.render()
      }
    }
    closeSnippet()
  }
}
