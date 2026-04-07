package com.fastscala.components.form7.fields

import com.fastscala.components.form7.*
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.mixins.mainelem.*
import com.fastscala.components.form7.mixins.mainelem.{F7FieldWithAdditionalAttrs, F7FieldWithDisabled}
import com.fastscala.components.form7.renderers.*
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.{Success, Try}
import scala.xml.{Elem, NodeSeq}

trait F7InputFieldBase[T]()(implicit val renderer: F7ValidatableFieldWithMainElemRenderer)
  extends F7FieldWithValue[T]
    with F7FieldWithoutChildren
    with F7FieldWithMainElemWithValidation
    with F7FieldSerializableAsString
    with F7FieldFocusableMainElem
    with F7FieldWithDisabled
    with F7FieldWithRequired
    with F7FieldWithReadOnly
    with F7FieldWithEnabled
    with F7FieldWithTabIndex
    with F7FieldWithName
    with F7FieldWithPlaceholder
    with F7FieldWithLabel
    with F7FieldWithMainElemId
    with F7FieldWithValidFeedback
    with F7FieldWithHelp
    with F7FieldWithMaxlength
    with F7FieldWithOnChangedField
    with F7FieldWithSyncToServerOnChange
    with F7FieldWithInputElemType
    with F7FieldWithAdditionalAttrs
    with F7FieldWithDependencies {

  def toString(value: T): String

  def fromString(str: String): Either[String, T]

  override def loadFromString(str: String): Seq[(F7Field, NodeSeq)] = {
    fromString(str) match {
      case Right(value) =>
        currentValue = value
        _setter(currentValue)
        Nil
      case Left(error) =>
        List((this, scala.xml.Text(s"Could not parse value '$str': $error")))
    }
  }

  override def saveToString(): Option[String] = Some(toString(currentValue)).filter(_ != "")

  override def submit()(implicit form: Form7, fsc: FSContext): Js = super.submit() & _setter(currentValue)

  override def updateFieldValueWithoutReRendering(previous: T, current: T)(implicit form: Form7, fsc: FSContext): Try[Js] =
    Success(JS.setElementValue(mainElemId, this.toString(currentValue)))

  protected def renderImpl()(implicit form: Form7, fsc: FSContext): Elem = {
    val errorsToShow: Seq[(F7Field, NodeSeq)] = if (shouldShowValidation_?) validate() else Nil
    showingValidation = errorsToShow.nonEmpty

    def sync(suggestSubmit: Boolean) = fsc.callback(
      JS.elementValueById(mainElemId),
      str => {
        fromString(str) match {
          case Right(value) =>
            setFilled()
            (if (currentValue != value) {
              currentValue = value
              _renderedValue.setRendered()
              form.onEvent(ChangedField(this))
            } else {
              JS.void
            })
              &
              (if (suggestSubmit) form.onEvent(SuggestSubmit(this)) else Js.Void)
          case Left(error) =>
            JS.void
        }
      }
    ).cmd

    renderer.render(this)(
      mainElem = processMainElem(<input
        id={id.getOrElse(null)}
        type={inputType}
        onblur={sync(false)}
        onchange={if (syncToServerOnChange) sync(false) else null}
        onkeypress={s"event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {${sync(true)}}"}
        value={this.toString(currentValue)}/>),
      label = this.label,
      invalidFeedback = errorsToShow.headOption.map(error => <div>
        {error._2}
      </div>),
      validFeedback = if (errorsToShow.isEmpty) validFeedback else None,
      help = help
    )
  }
}
