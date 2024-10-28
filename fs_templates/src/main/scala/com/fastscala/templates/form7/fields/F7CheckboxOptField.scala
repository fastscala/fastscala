package com.fastscala.templates.form7.fields

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7._
import com.fastscala.templates.form7.mixins._
import com.fastscala.templates.form7.renderers._
import com.fastscala.xml.scala_xml.JS

import scala.xml.{Elem, NodeSeq}

class F7CheckboxOptField()(implicit val renderer: CheckboxF7FieldRenderer) extends StandardOneInputElemF7Field[Option[Boolean]]
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
  with F7FieldWithDependencies {

  override def defaultValue: Option[Boolean] = None

  override def loadFromString(str: String): Seq[(F7Field, NodeSeq)] = {
    currentValue = str.toBooleanOption
    _setter(currentValue)
    Nil
  }

  override def saveToString(): Option[String] = currentValue.map(_.toString)

  override def submit()(implicit form: Form7, fsc: FSContext): Js = super.submit() & _setter(currentValue)

  def focusJs: Js = Js.focus(elemId) & Js.select(elemId)

  override def postRenderSetupJs()(implicit fsc: FSContext): Js = if (currentValue == None) {
    JS.setIndeterminate(elemId)
  } else Js.void

  override def updateFieldStatus()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = super.updateFieldStatus() &
    Js.setCheckboxTo(elemId, currentValue)

  def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    if (!enabled()) renderer.renderDisabled(this)
    else {
      withFieldRenderHints { implicit hints =>

        val errorsToShow: Seq[(F7Field, NodeSeq)] = if (shouldShowValidation_?) validate() else Nil
        showingValidation = errorsToShow.nonEmpty

        val onchangeJs = fsc.callback(() => {
          setFilled()
          currentValue match {
            case Some(false) => currentValue = None
            case None => currentValue = Some(true)
            case Some(true) => currentValue = Some(false)
          }
          currentRenderedValue = Some(currentValue)
          form.onEvent(ChangedField(this)) & reRender()
        }).cmd

        currentRenderedValue = Some(currentValue)

        renderer.render(this)(
          inputElem = processInputElem(
            <input type="checkbox"
                   id={elemId}
                   onchange={onchangeJs}
                   checked={if (currentRenderedValue.get == Some(true)) "true" else null}
            ></input>
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
