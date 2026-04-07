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

class F7CheckboxOptField()(implicit val renderer: F7CheckboxFieldRenderer)
  extends F7FieldWithValue[Option[Boolean]]
    with F7FieldWithoutChildren
    with F7FieldWithMainElemWithValidation
    with F7Field
    with F7FieldSerializableAsString
    with F7FieldFocusableMainElem
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

  var _switchingToUndefinedAllowed: () => Boolean = () => true

  def switchingToUndefinedAllowed: Boolean = _switchingToUndefinedAllowed()

  def allowSwitchingToUndefined: this.type = switchingToUndefinedAllowed(true)

  def disableSwitchingToUndefined: this.type = switchingToUndefinedAllowed(false)

  def switchingToUndefinedAllowed(v: Boolean): this.type = mutate {
    _switchingToUndefinedAllowed = () => v
  }

  def switchingToUndefinedAllowed(f: () => Boolean): this.type = mutate {
    _switchingToUndefinedAllowed = f
  }

  override def defaultValue: Option[Boolean] = None

  override def loadFromString(str: String): Seq[(F7Field, NodeSeq)] = {
    currentValue = str.toBooleanOption
    _setter(currentValue)
    Nil
  }

  override def saveToString(): Option[String] = currentValue.map(_.toString)

  override def submit()(implicit form: Form7, fsc: FSContext): Js = super.submit() & _setter(currentValue)

  override def postRenderSetupJs()(implicit fsc: FSContext): Js = if (currentValue == None) {
    JS.setIndeterminate(mainElemId)
  } else JS.void

  override def updateFieldValueWithoutReRendering(previous: Option[Boolean], current: Option[Boolean])(implicit form: Form7, fsc: FSContext): Try[Js] =
    Success(JS.setCheckboxTo(mainElemId, currentValue))

  protected def renderImpl()(implicit form: Form7, fsc: FSContext): Elem = {
    val errorsToShow: Seq[(F7Field, NodeSeq)] = if (shouldShowValidation_?) validate() else Nil
    showingValidation = errorsToShow.nonEmpty

    val onchangeJs = fsc.callback(() => {
      setFilled()
      currentValue match {
        case Some(false) if switchingToUndefinedAllowed => currentValue = None
        case Some(false) if !switchingToUndefinedAllowed => currentValue = Some(true)
        case None => currentValue = Some(true)
        case Some(true) => currentValue = Some(false)
      }
      _renderedValue.setRendered()
      form.onEvent(ChangedField(this)) & reRender()
    }).cmd

    renderer.render(this)(
      mainElem = processMainElem(<input type="checkbox"
                   id={id.getOrElse(null)}
                   onchange={onchangeJs}
                   checked={if (currentValue == Some(true)) "true" else null}
            ></input>),
      label = this.label,
      invalidFeedback = errorsToShow.headOption.map(error => <div>{error._2}</div>),
      validFeedback = if (errorsToShow.isEmpty) validFeedback else None,
      help = help
    )
  }
}
