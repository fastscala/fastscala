package com.fastscala.components.form7.fields.radio

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.form7.*
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.renderers.*
import com.fastscala.utils.IdGen

import scala.util.chaining.scalaUtilChainingOps
import scala.util.{Failure, Success}
import scala.xml.{Elem, NodeSeq}

abstract class F7RadioFieldBase[T]()(implicit val renderer: RadioF7FieldRenderer) extends StandardF7Field
  with StringSerializableF7Field
  with FocusableF7Field
  with F7FieldWithOptions[T]
  with F7FieldWithOptionIds[T]
  with F7FieldWithDisabled
  with F7FieldWithRequired
  with F7FieldWithReadOnly
  with F7FieldWithEnabled
  with F7FieldWithTabIndex
  with F7FieldWithValidFeedback
  with F7FieldWithHelp
  with F7FieldWithName
  with F7FieldWithLabel
  with F7FieldWithAdditionalAttrs
  with F7FieldWithDependencies
  with F7FieldWithValue[T]
  with F7FieldWithOptionsNsLabel[T] {

  val radioNameId = IdGen.id

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

  override def updateFieldDisabledStatus()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = _disabled().pipe(shouldBeDisabled => {
    if (shouldBeDisabled != currentlyDisabled) {
      currentlyDisabled = shouldBeDisabled
      if (currentlyReadOnly) {
        JS.apply(s"""Array.from(document.querySelectorAll('#${aroundId} [name=$radioNameId]')).forEach((elem,idx) => { elem.setAttribute('disabled', 'disabled') });""")
      } else {
        JS.apply(s"""Array.from(document.querySelectorAll('#${aroundId} [name=$radioNameId]')).forEach((elem,idx) => { elem.removeAttribute('disabled') });""")
      }
    } else {
      JS.void
    }
  })

  override def updateFieldReadOnlyStatus()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = _readOnly().pipe(shouldBeReadOnly => {
    if (shouldBeReadOnly != currentlyReadOnly) {
      currentlyReadOnly = shouldBeReadOnly
      if (currentlyReadOnly) {
        JS.apply(s"""Array.from(document.querySelectorAll('#${aroundId} [name=$radioNameId]')).forEach((elem,idx) => { elem.setAttribute('readonly', 'true') });""")
      } else {
        JS.apply(s"""Array.from(document.querySelectorAll('#${aroundId} [name=$radioNameId]')).forEach((elem,idx) => { elem.removeAttribute('readonly') });""")
      }
    } else {
      JS.void
    }
  })

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().flatMap(superJs =>
      currentRenderedOptions.map({
        // The value rendered on the client side is different from the one on the server side:
        case (renderedOptions, ids2Option, option2Id) if !currentRenderedValue.exists(_ == currentValue) =>
          this.currentRenderedValue = Some(currentValue)
          option2Id.get(currentValue).map(optionId => Success(superJs & JS.setChecked(optionId, true)))
            .getOrElse(Failure(new Exception("CurrentValue is not one of the rendered values")))
        case _ => Success(superJs)
      }).getOrElse(Success(superJs)))

  def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    if (!enabled) renderer.renderDisabled(this)
    else {
      withFieldRenderHints { implicit hints =>

        val errorsToShow: Seq[(F7Field, NodeSeq)] = if (shouldShowValidation_?) validate() else Nil
        showingValidation = errorsToShow.nonEmpty

        val renderedOptions: Seq[T] = options
        val ids2Option: Map[String, T] = renderedOptions.map(opt => fsc.session.nextID() -> opt).toMap
        val option2Id: Map[T, String] = ids2Option.map(_.swap)

        if (!renderedOptions.contains(currentValue)) currentValue = defaultValue

        currentRenderedValue = Some(currentValue)
        currentRenderedOptions = Some((renderedOptions, ids2Option, option2Id))

        val radioToggles: Seq[(Elem, Some[Elem])] = renderedOptions.map(opt => {
          val onchange = fsc.callback(() => {
            currentRenderedValue = Some(opt)
            if (currentValue != opt) {
              setFilled()
              currentValue = opt
              form.onEvent(ChangedField(this))
            } else {
              JS.void
            }
          }).cmd
          (processInputElem(<input id={option2Id(opt)} checked={if (currentValue == opt) "checked" else null} onchange={onchange} type="radio" name={radioNameId}></input>), Some(<label>{_option2NodeSeq(opt)}</label>))
        })

        renderer.render(this)(
          inputElemsAndLabels = radioToggles,
          label = _label(),
          invalidFeedback = errorsToShow.headOption.map(error => <div>{error._2}</div>),
          validFeedback = if (errorsToShow.isEmpty) validFeedback else None,
          help = help
        )
      }
    }
  }

  override def showOrUpdateValidation(ns: NodeSeq): Js = renderer.showOrUpdateValidation(this)(ns)

  override def hideValidation(): Js = renderer.hideValidation(this)()
}
