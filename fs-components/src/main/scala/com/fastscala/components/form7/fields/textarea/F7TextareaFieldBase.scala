package com.fastscala.components.form7.fields.text

import com.fastscala.components.form7.*
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.renderers.*
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.xml.{Elem, NodeSeq}

abstract class F7TextareaFieldBase[T]()(implicit val renderer: TextareaF7FieldRenderer)
    extends StandardOneInputElemF7Field[T]
    with F7Field
    with StringSerializableF7Field
    with FocusableF7Field
    with F7FieldWithNumRows
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

      renderer.render(this)(
        inputElem = processInputElem(<textarea
          type="text"
          id={id.getOrElse(null)}
          onblur={
            fsc.callback(
              JS.elementValueById(elemId),
              str => {
                fromString(str) match {
                  case Right(value) =>
                    setFilled()
                    currentRenderedValue = Some(value)
                    if (currentValue != value) {
                      currentValue = value
                      form.onEvent(ChangedField(this))
                    } else {
                      JS.void
                    }
                  case Left(error) =>
                    JS.void
                }
              }
            ).cmd
          }>{this.toString(currentRenderedValue.get)}</textarea>),
        label = _label(),
        invalidFeedback = errorsToShow.headOption.map(error => <div>
          {error._2}
        </div>),
        validFeedback = if (errorsToShow.isEmpty) validFeedback else None,
        help = help
      )
    }
  }
}
