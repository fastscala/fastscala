package com.fastscala.components.form7.fields.checkbox

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

class F7CheckboxField()(implicit val renderer: F7CheckboxFieldRenderer)
  extends F7FieldWithValue[Boolean]
    with F7FieldWithoutChildren
    with F7FieldFocusableMainElem
    with F7FieldWithMainElemWithValidation
    with F7Field
    with F7FieldSerializableAsString
    with F7FieldFocusable
    with F7FieldWithDisabled
    with F7FieldWithReadOnly
    with F7FieldWithEnabled
    with F7FieldWithTabIndex
    with F7FieldWithName
    with F7FieldWithValidFeedback
    with F7FieldWithHelp
    with F7FieldWithLabel
    with F7FieldWithMainElemId
    with F7FieldWithAdditionalAttrs
    with F7FieldWithDependencies {

  override def defaultValue: Boolean = false

  override def loadFromString(str: String): Seq[(F7Field, NodeSeq)] = str.toBooleanOption match {
    case Some(value) =>
      currentValue = value
      _setter(currentValue)
      Nil
    case None =>
      List((this, scala.xml.Text(s"Could not parse value '$str' as boolean")))
  }

  override def saveToString(): Option[String] = Some(currentValue.toString).filter(_ != "")

  override def submit()(implicit form: Form7, fsc: FSContext): Js = super.submit() & _setter(currentValue)

  override def updateFieldValueWithoutReRendering(previous: Boolean, current: Boolean)(implicit form: Form7, fsc: FSContext): Try[Js] =
    Success(JS.setChecked(mainElemId, current))

  protected def renderImpl()(implicit form: Form7, fsc: FSContext): Elem = {
    val errorsToShow: Seq[(F7Field, NodeSeq)] = if (shouldShowValidation_?) validate() else Nil
    showingValidation = errorsToShow.nonEmpty

    val onchangeJs = fsc.callback(
      JS.isCheckedById(mainElemId),
      str => {
        str.toBooleanOption match {
          case Some(value) if currentValue == value => JS.void
          case Some(value) =>
            if (currentValue != value) {
              setFilled()
              currentValue = value
              _renderedValue.setRendered()
              form.onEvent(ChangedField(this))
            } else {
              JS.void
            }
          case None =>
            // Log error
            JS.void
        }
      }
    ).cmd

    renderer.render(this)(
      mainElem = processMainElem(<input type="checkbox"
                      id={id.getOrElse(null)}
                      onchange={onchangeJs}
                      checked={if (currentValue) "true" else null}
            ></input>),
      label = this.label,
      invalidFeedback = errorsToShow.headOption.map(error => <div>{error._2}</div>),
      validFeedback = if (errorsToShow.isEmpty) validFeedback else None,
      help = help
    )
  }
}
