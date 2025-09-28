package com.fastscala.components.form7.fields

import com.fastscala.components.form7.*
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.renderers.*
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.xml.{Elem, NodeSeq}

class F7CheckboxOptField()(implicit val renderer: CheckboxF7FieldRenderer)
    extends StandardOneInputElemF7Field[Option[Boolean]]
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
    with F7FieldWithId
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

  def focusJs: Js = JS.focus(elemId) & JS.select(elemId)

  override def postRenderSetupJs()(implicit fsc: FSContext): Js = if (currentValue == None) {
    JS.setIndeterminate(elemId)
  } else JS.void

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(
      _ &
        JS.setCheckboxTo(elemId, currentValue)
    )

  def render()(implicit form: Form7, fsc: FSContext): Elem = {
    if (!enabled) renderer.renderDisabled(this)
    else {
      val errorsToShow: Seq[(F7Field, NodeSeq)] = if (shouldShowValidation_?) validate() else Nil
      showingValidation = errorsToShow.nonEmpty

      val onchangeJs = fsc.callback(() => {
        setFilled()
        currentValue match {
          case Some(false) if switchingToUndefinedAllowed  => currentValue = None
          case Some(false) if !switchingToUndefinedAllowed => currentValue = Some(true)
          case None                                        => currentValue = Some(true)
          case Some(true)                                  => currentValue = Some(false)
        }
        currentRenderedValue = Some(currentValue)
        form.onEvent(ChangedField(this)) & reRender()
      }).cmd

      currentRenderedValue = Some(currentValue)

      renderer.render(this)(
        inputElem = processInputElem(<input type="checkbox"
                   id={id.getOrElse(null)}
                   onchange={onchangeJs}
                   checked={if (currentRenderedValue.get == Some(true)) "true" else null}
            ></input>),
        label = _label(),
        invalidFeedback = errorsToShow.headOption.map(error => <div>{error._2}</div>),
        validFeedback = if (errorsToShow.isEmpty) validFeedback else None,
        help = help
      )
    }
  }
}
