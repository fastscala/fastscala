package com.fastscala.templates.form7.fields.radio

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7._
import com.fastscala.templates.form7.fields.text.F7FieldWithAdditionalAttrs
import com.fastscala.templates.form7.mixins._
import com.fastscala.templates.form7.renderers._
import com.fastscala.utils.IdGen
import com.fastscala.xml.scala_xml.FSScalaXmlSupport

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
    val all = options()
    all.find({
      case opt => _option2Id(opt, all) == str
    }) match {
      case Some(v) =>
        currentValue = v
        _setter(v)
        Nil
      case None =>
        List((this, FSScalaXmlSupport.fsXmlSupport.buildText(s"Not found id: '$str'")))
    }
  }

  override def saveToString(): Option[String] = Some(_option2Id(currentValue, options())).filter(_ != "")

  override def submit()(implicit form: Form7, fsc: FSContext): Js = super.submit() & _setter(currentValue)

  def focusJs: Js = Js.focus(elemId) & Js.select(elemId)

  def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    if (!enabled()) renderer.renderDisabled(this)
    else {
      withFieldRenderHints { implicit hints =>

        val errorsToShow: Seq[(F7Field, NodeSeq)] = if (shouldShowValidation_?) validate() else Nil
        showingValidation = errorsToShow.nonEmpty

        val renderedOptions = options()
        val radioToggles: Seq[(Elem, Some[Elem])] = renderedOptions.map(opt => {
          val onchange = fsc.callback(() => {
            if (currentValue != opt) {
              setFilled()
              currentValue = opt
              form.onEvent(ChangedField(this))
            } else {
              Js.void
            }
          }).cmd
          (processInputElem(<input checked={if (currentValue == opt) "checked" else null} onchange={onchange} type="radio" name={radioNameId}></input>), Some(<label>{_option2NodeSeq(opt)}</label>))
        })

        renderer.render(this)(
          inputElemsAndLabels = radioToggles,
          invalidFeedback = errorsToShow.headOption.map(error => <div>{error._2}</div>),
          validFeedback = if (errorsToShow.isEmpty) validFeedback() else None,
          help = help()
        )
      }
    }
  }

  override def showOrUpdateValidation(ns: NodeSeq): Js = renderer.showOrUpdateValidation(this)(ns)

  override def hideValidation(): Js = renderer.hideValidation(this)
}
