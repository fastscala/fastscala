package com.fastscala.templates.form7.fields

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7._
import com.fastscala.templates.form7.fields.text.F7FieldWithAdditionalAttrs
import com.fastscala.templates.form7.mixins._
import com.fastscala.templates.form7.renderers._
import com.fastscala.xml.scala_xml.FSScalaXmlSupport

import scala.xml.{Elem, NodeSeq}

class F7CheckboxField()(implicit val renderer: CheckboxF7FieldRenderer) extends StandardOneInputElemF7Field
  with F7Field
  with StringSerializableF7Field
  with FocusableF7Field
  with F7FieldWithDisabled
  with F7FieldWithReadOnly
  with F7FieldWithEnabled
  with F7FieldWithTabIndex
  with F7FieldWithName
  with F7FieldWithValidFeedback
  with F7FieldWithHelp
  with F7FieldWithLabel
  with F7FieldWithAdditionalAttrs
  with F7FieldWithDependencies
  with F7FieldWithValue[Boolean] {

  override def defaultValue: Boolean = false

  override def loadFromString(str: String): Seq[(F7Field, NodeSeq)] = str.toBooleanOption match {
    case Some(value) =>
      currentValue = value
      _setter(currentValue)
      Nil
    case None =>
      List((this, FSScalaXmlSupport.fsXmlSupport.buildText(s"Could not parse value '$str' as boolean")))
  }


  override def saveToString(): Option[String] = Some(currentValue.toString).filter(_ != "")

  override def submit()(implicit form: Form7, fsc: FSContext): Js = super.submit() & _setter(currentValue)

  def focusJs: Js = Js.focus(elemId) & Js.select(elemId)

  def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    if (!enabled()) renderer.renderDisabled(this)
    else {
      withFieldRenderHints { implicit hints =>

        val errorsToShow: Seq[(F7Field, NodeSeq)] = if (shouldShowValidation_?) validate() else Nil
        showingValidation = errorsToShow.nonEmpty

        val onchangeJs = fsc.callback(Js.checkboxIsCheckedById(elemId), str => {
          str.toBooleanOption match {
            case Some(value) if currentValue != value =>
              setFilled()
              currentValue = value
              form.onEvent(ChangedField(this))
            case Some(value) if currentValue == value => Js.void
            case None =>
              // Log error
              Js.void
          }
        }).cmd

        renderer.render(this)(
          inputElem = processInputElem(
            <input type="checkbox"
                   id={elemId}
                   onchange={onchangeJs}
                   checked={if (currentValue) "true" else null}
            ></input>
          ),
          label = _label(),
          invalidFeedback = errorsToShow.headOption.map(error => <div>{error._2}</div>),
          validFeedback = if (errorsToShow.isEmpty) validFeedback() else None,
          help = help()
        )
      }
    }
  }
}
