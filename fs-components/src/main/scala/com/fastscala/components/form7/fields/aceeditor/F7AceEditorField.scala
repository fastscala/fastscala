package com.fastscala.components.form7.fields.aceeditor

import com.fastscala.components.aceeditor.json.BindKey
import com.fastscala.components.aceeditor.{AceEditor, Language, Theme}
import com.fastscala.components.bootstrap5.components.BSBtnDropdown
import com.fastscala.components.bootstrap5.helpers.BSHelpers.s
import com.fastscala.components.bootstrap5.utils.BSBtn
import com.fastscala.components.form7.{ChangedField, F7Field, Form7, SuggestSubmit}
import com.fastscala.components.form7.fields.F7InputFieldBase
import com.fastscala.components.form7.fields.text.F7TextareaFieldBase
import com.fastscala.components.form7.mixins.mainelem.*
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.renderers.*
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.{JS, inScriptTag}

import java.nio.charset.StandardCharsets
import scala.util.Try
import scala.xml.{Elem, NodeSeq}

class F7AceEditorField(implicit val renderer: F7AceEditorValidatableFieldRenderer) extends F7FieldWithValue[String]
  with F7FieldWithoutChildren
  with F7FieldWithMainElemWithValidation
  with F7FieldSerializableAsString
  with F7FieldWithValidation
  with F7FieldWithEnabled
  with F7FieldWithNumRows
  with F7FieldWithLabel
  with F7FieldWithMainElemId
  with F7FieldWithValidFeedback
  with F7FieldWithHelp
  with F7FieldWithOnChangedField
  with F7FieldWithDependencies
  with F7AceEditorFieldWithLanguage
  with F7AceEditorFieldWithTheme {

  override def disabled: Boolean = false

  override def defaultValue: String = ""

  def toString(value: String): String = value

  def fromString(str: String): Either[String, String] = Right(str)

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  private var currentAceEditor = Option.empty[AceEditor]

  override def loadFromString(str: String): Seq[(F7Field, NodeSeq)] = {
    currentValue = str
    _setter(currentValue)
    Nil
  }

  override def submit()(implicit form: Form7, fsc: FSContext): Js = super.submit() & _setter(currentValue)

  override def updateFieldValueWithoutReRendering(previous: String, current: String)(implicit form: Form7, fsc: FSContext): Try[Js] = ???

  override def saveToString(): Option[String] = Some(currentValue)

  override def defaultNumRows: Option[Int] = Some(12)

  override protected def renderImpl()(implicit form: Form7, fsc: FSContext): Elem = {
    val errorsToShow: Seq[(F7Field, NodeSeq)] = if (shouldShowValidation_?) validate() else Nil
    showingValidation = errorsToShow.nonEmpty

    val field = this
    val aceEditor = new AceEditor() {
      override def initalValue: String = field.currentValue

      override def defaultMode: Language.Value = language

      override def defaultTheme: Theme.Value = theme

      override def defaultMinLines: Option[Int] = numRows

      override def defaultMaxLines: Option[Int] = numRows

      override def onChangeServerSide(browserValue: String)(implicit fsc: FSContext): Js = super.onChangeServerSide(browserValue) & {
        setFilled()
        if (field.currentValue != browserValue) {
          field.currentValue = browserValue
          _renderedValue.setRendered()
          form.onEvent(ChangedField(field))
        } else {
          JS.void
        }
      }

      override def initialize()(implicit fsc: FSContext): Js = super.initialize() & {
        addCommand("Save", BindKey("Ctrl-s", "Command-s"), implicit fsc => JS.function1(_ => fsc.callback(() => form.onEvent(SuggestSubmit(field)))))
      }
    }
    currentAceEditor = Some(aceEditor)

    renderer.render(this)(
      aceEditor = aceEditor,
      label = this.label,
      invalidFeedback = errorsToShow.headOption.map(error => <div>{error._2}</div>),
      validFeedback = if (errorsToShow.isEmpty) validFeedback else None,
      help = help
    )
  }
}
