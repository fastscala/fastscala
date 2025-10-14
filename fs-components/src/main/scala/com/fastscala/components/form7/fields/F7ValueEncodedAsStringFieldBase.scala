package com.fastscala.components.form7.fields

import com.fastscala.components.form7.*
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.renderers.*
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.xml.{Elem, NodeSeq}

abstract class F7ValueEncodedAsStringFieldBase[T]()(implicit val renderer: TextF7FieldRenderer)
    extends StandardOneInputElemF7Field[T]
    with StringSerializableF7Field
    with FocusableF7Field
    with F7FieldWithDisabled
    with F7FieldWithRequired
    with F7FieldWithReadOnly
    with F7FieldWithEnabled
    with F7FieldWithTabIndex
    with F7FieldWithName
    with F7FieldWithPlaceholder
    with F7FieldWithLabel
    with F7FieldWithId
    with F7FieldWithValidFeedback
    with F7FieldWithHelp
    with F7FieldWithMaxlength
    with F7FieldWithOnChangedField
    with F7FieldWithSyncToServerOnChange
    with F7FieldWithInputType
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

  def focusJs: Js = JS.focus(elemId) & JS.select(elemId)

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(
      _ &
        currentRenderedValue.filter(_ != currentValue).map(currentRenderedValue => {
          this.currentRenderedValue = Some(currentValue)
          JS.setElementValue(elemId, this.toString(currentValue))
        }).getOrElse(JS.void)
    )

  def render()(implicit form: Form7, fsc: FSContext): Elem = {
    if (!enabled) renderer.renderDisabled(this)
    else {
      val errorsToShow: Seq[(F7Field, NodeSeq)] = if (shouldShowValidation_?) validate() else Nil
      showingValidation = errorsToShow.nonEmpty

      currentRenderedValue = Some(currentValue)

      def sync(suggestSubmit: Boolean) = fsc.callback(
        JS.elementValueById(elemId),
        str => {
          fromString(str) match {
            case Right(value) =>
              setFilled()
              currentRenderedValue = Some(value)
              (if (currentValue != value) {
                 currentValue = value
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
        inputElem = processInputElem(<input
              id={id.getOrElse(null)}
              type={inputType}
              onblur={sync(false)}
              onchange={if (syncToServerOnChange) sync(false) else null}
              onkeypress={s"event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {${sync(true)}}"}
              value={this.toString(currentRenderedValue.get)}
            />),
        label = _label(),
        invalidFeedback = errorsToShow.headOption.map(error => <div>{error._2}</div>),
        validFeedback = if (errorsToShow.isEmpty) validFeedback else None,
        help = help
      )
    }
  }

  def inputTypeEmail = super.inputType("email")

  def inputTypePassword = super.inputType("password")

  def inputTypeTel = super.inputType("tel")

  def inputTypeUrl = super.inputType("url")
}
