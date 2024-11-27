package com.fastscala.components.form7.fields.select

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.form7.*
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.renderers.*

import scala.util.{Failure, Success}
import scala.xml.{Elem, NodeSeq}

abstract class F7SelectFieldBase[T]()(implicit val renderer: SelectF7FieldRenderer) extends StandardOneInputElemF7Field[T]
  with F7FieldWithOptions[T]
  with F7FieldWithOptionIds[T]
  with F7Field
  with StringSerializableF7Field
  with FocusableF7Field
  with F7FieldWithDisabled
  with F7FieldWithRequired
  with F7FieldWithReadOnly
  with F7FieldWithEnabled
  with F7FieldWithOnChangedField
  with F7FieldWithTabIndex
  with F7FieldWithValidFeedback
  with F7FieldWithHelp
  with F7FieldWithName
  with F7FieldWithLabel
  with F7FieldWithAdditionalAttrs
  with F7FieldWithDependencies
  with F7FieldWithOptionsNsLabel[T] {

  override def loadFromString(str: String): Seq[(F7Field, NodeSeq)] = {
    val all = options
    all.find({
      case opt => _option2Id(opt, all) == str
    }) match {
      case Some(v) =>
        currentValue = v
        _setter(v)
        Nil
      case None =>
        List((this, scala.xml.Text(s"Not found id: '$str'")))
    }
  }

  override def saveToString(): Option[String] = Some(_option2Id(currentValue, options)).filter(_ != "")

  override def submit()(implicit form: Form7, fsc: FSContext): Js = super.submit() & _setter(currentValue)

  def focusJs: Js = JS.focus(elemId) & JS.select(elemId)

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().flatMap(superJs => {
      currentRenderedOptions.map({
        // The value rendered on the client side is different from the one on the server side:
        case (renderedOptions, ids2Option, option2Id) if !currentRenderedValue.exists(_ == currentValue) =>
          option2Id.get(currentValue).map(valueId => {
            this.currentRenderedValue = Some(currentValue)
            Success(superJs & JS.setElementValue(elemId, valueId))
          }).getOrElse(Failure(new Exception("CurrentValue is not one of the rendered values")))
        case _ => Success(superJs)
      }).getOrElse(Success(superJs))
    })

  def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    if (!enabled) renderer.renderDisabled(this)
    else {
      withFieldRenderHints { implicit hints =>

        val errorsToShow: Seq[(F7Field, NodeSeq)] = if (shouldShowValidation_?) validate() else Nil
        showingValidation = errorsToShow.nonEmpty

        val renderedOptions: Seq[T] = options
        val ids2Option: Map[String, T] = renderedOptions.map(opt => fsc.session.nextID() -> opt).toMap
        val option2Id: Map[T, String] = ids2Option.map(_.swap)
        currentRenderedOptions = Some((renderedOptions, ids2Option, option2Id))

        if (!renderedOptions.contains(currentValue)) currentValue = defaultValue

        currentRenderedValue = Some(currentValue)
        val optionsRendered = renderedOptions.map(opt => {
          renderer.renderOption(currentValue == opt, option2Id(opt), _option2NodeSeq(opt))
        })

        val onchangeJs = fsc.callback(JS.elementValueById(elemId), id => {
          ids2Option.get(id) match {
            case Some(value) if currentValue == value => JS.void
            case Some(value) =>
              currentRenderedValue = Some(value)
              if (currentValue != value) {
                setFilled()
                currentValue = value
                form.onEvent(ChangedField(this))
              } else JS.void
            case None =>
              // Log error
              JS.void
          }
        }).cmd

        renderer.render(this)(
          inputElem = processInputElem(
            <select
              onblur={onchangeJs}
              onchange={onchangeJs}
            >{optionsRendered}</select>
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
