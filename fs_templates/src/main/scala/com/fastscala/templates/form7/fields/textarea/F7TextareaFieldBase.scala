package com.fastscala.templates.form7.fields.text

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.templates.form7.*
import com.fastscala.templates.form7.mixins.*
import com.fastscala.templates.form7.renderers.*
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.{Elem, NodeSeq}

abstract class F7TextareaFieldBase[T]()(implicit val renderer: TextareaF7FieldRenderer) extends StandardOneInputElemF7Field[T]
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

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ &
      currentRenderedValue.filter(_ != currentValue).map(currentRenderedValue => {
        this.currentRenderedValue = Some(currentValue)
        JS.setElementValue(elemId, this.toString(currentValue))
      }).getOrElse(JS.void))

  def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    if (!enabled) renderer.renderDisabled(this)
    else
      withFieldRenderHints { implicit hints =>

        val errorsToShow: Seq[(F7Field, NodeSeq)] = if (shouldShowValidation_?) validate() else Nil
        showingValidation = errorsToShow.nonEmpty

        currentRenderedValue = Some(currentValue)

        renderer.render(this)(
          inputElem = processInputElem(
            <textarea
                      type="text"
                      onblur={
                      fsc.callback(JS.elementValueById(elemId), str => {
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
                      }).cmd
                      }
                      onkeypress={s"event = event || window.event; if ((event.keyCode ? event.keyCode : event.which) == 13 && event.ctrlKey) {${JS.evalIf(hints.contains(SaveOnEnterHint))(JS.blur(elemId) & form.submitFormClientSide())}}"}
          >{this.toString(currentRenderedValue.get)}</textarea>
          ),
          label = _label(),
          invalidFeedback = errorsToShow.headOption.map(error => <div>{error._2}</div>),
          validFeedback = if (errorsToShow.isEmpty) validFeedback else None,
          help = help
        )
      }
  }
}
