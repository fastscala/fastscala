package com.fastscala.templates.form7.fields.text

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7._
import com.fastscala.templates.form7.mixins._
import com.fastscala.templates.form7.renderers._
import com.fastscala.xml.scala_xml.FSScalaXmlSupport
import com.fastscala.xml.scala_xml.JS.RichJs

import scala.xml.{Elem, NodeSeq}


abstract class F7TextFieldBase[T]()(implicit val renderer: TextF7FieldRenderer) extends StandardOneInputElemF7Field[T]
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
        List((this, FSScalaXmlSupport.fsXmlSupport.buildText(s"Could not parse value '$str': $error")))
    }
  }

  override def saveToString(): Option[String] = Some(toString(currentValue)).filter(_ != "")

  override def submit()(implicit form: Form7, fsc: FSContext): Js = super.submit() & _setter(currentValue)

  def focusJs: Js = Js.focus(elemId) & Js.select(elemId)

  override def updateFieldStatus()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js =
    super.updateFieldStatus() &
      currentRenderedValue.filter(_ != currentValue).map(currentRenderedValue => {
        this.currentRenderedValue = Some(currentValue)
        Js.setElementValue(elemId, this.toString(currentValue))
      }).getOrElse(Js.void)

  def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    if (!enabled()) renderer.renderDisabled(this)
    else {
      withFieldRenderHints { implicit hints =>

        val errorsToShow: Seq[(F7Field, NodeSeq)] = if (shouldShowValidation_?) validate() else Nil
        showingValidation = errorsToShow.nonEmpty

        currentRenderedValue = Some(currentValue)

        val onChange = fsc.callback(Js.elementValueById(elemId), str => {
          fromString(str) match {
            case Right(value) =>
              setFilled()
              currentRenderedValue = Some(value)
              if (currentValue != value) {
                currentValue = value
                form.onEvent(ChangedField(this))
              } else {
                Js.void
              }
            case Left(error) =>
              Js.void
          }
        }).cmd

        renderer.render(this)(
          inputElem = processInputElem(
            <input
              type={inputType}
              onblur={onChange}
              onchange={if (syncToServerOnChange) onChange else null}
              onkeypress={s"event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13) {${Js.evalIf(hints.contains(SaveOnEnterHint))(Js.blur(elemId) & form.submitFormClientSide())}}"}
              value={this.toString(currentRenderedValue.get)}
            />
          ),
          label = _label(),
          invalidFeedback = errorsToShow.headOption.map(error => <div>{error._2}</div>),
          validFeedback = if (errorsToShow.isEmpty) validFeedback else None,
          help = help
        )
      }
    }
  }
}
