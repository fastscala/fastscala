package com.fastscala.components.form7.fields.multiselect

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.form7.*
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.renderers.*
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.{Elem, NodeSeq}

abstract class F7MultiSelectFieldBase[T]()(implicit val renderer: MultiSelectF7FieldRenderer) extends StandardOneInputElemF7Field[Set[T]]
  with F7FieldWithOptions[T]
  with F7FieldWithOptionIds[T]
  with F7Field
  with StringSerializableF7Field
  with FocusableF7Field
  with F7FieldWithDisabled
  with F7FieldWithRequired
  with F7FieldWithReadOnly
  with F7FieldWithEnabled
  with F7FieldWithTabIndex
  with F7FieldWithName
  with F7FieldWithSize
  with F7FieldWithValidFeedback
  with F7FieldWithHelp
  with F7FieldWithLabel
  with F7FieldWithAdditionalAttrs
  with F7FieldWithDependencies
  with F7FieldWithOptionsNsLabel[T] {

  override def loadFromString(str: String): Seq[(F7Field, NodeSeq)] = {
    val all = options
    val id2Option: Map[String, T] = all.map(opt => _option2Id(opt, all) -> opt).toMap
    val selected: Seq[T] = str.split(";").toList.flatMap(id => {
      id2Option.get(id)
    })
    currentValue = selected.toSet
    _setter(currentValue)
    Nil
  }

  override def saveToString(): Option[String] = Some(currentValue.map(opt => _option2Id(opt, options)).mkString(";"))

  override def submit()(implicit form: Form7, fsc: FSContext): Js = super.submit() & _setter(currentValue)

  def focusJs: Js = JS.focus(elemId) & JS.select(elemId)

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ &
      currentRenderedOptions.flatMap({
        case (renderedOptions, ids2Option, option2Id) if !currentRenderedValue.exists(_ == currentValue) =>
          this.currentRenderedValue = Some(currentValue)
          val selectedIndexes = renderedOptions.zipWithIndex.filter(e => currentValue.contains(e._1)).map(_._2)
          Some(JS {
            s"""var element = document.getElementById('${elemId}');
               |var selected = [${selectedIndexes.mkString(",")}];
               |for (var i = 0; i < element.options.length; i++) {
               |    element.options[i].selected = selected.includes(i);
               |}""".stripMargin
          })
        case _ => Some(JS.void)
      }).getOrElse(JS.void)
    )

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

        currentValue = currentValue.filter(!renderedOptions.contains(_))

        currentRenderedValue = Some(currentValue)
        val optionsRendered = renderedOptions.map(opt => {
          renderer.renderOption(currentValue.contains(opt), option2Id(opt), _option2NodeSeq(opt))
        })

        val onchangeJs = fsc.callback(JS.selectedValues(JS.elementById(elemId)), ids => {
          val value = ids.split(",").filter(_.trim != "").toSet[String].map(id => ids2Option(id))
          if (currentValue != value) {
            setFilled()
            currentRenderedValue = Some(value)
            currentValue = value
            form.onEvent(ChangedField(this))
          } else {
            JS.void
          }
        }).cmd

        renderer.render(this)(
          inputElem = processInputElem(
            <select
              multiple="multiple"
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
