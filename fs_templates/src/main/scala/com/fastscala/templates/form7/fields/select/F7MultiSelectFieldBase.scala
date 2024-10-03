package com.fastscala.templates.form7.fields.select


import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7._
import com.fastscala.templates.form7.fields.text.F7FieldWithAdditionalAttrs
import com.fastscala.templates.form7.mixins._
import com.fastscala.templates.form7.renderers._

import scala.xml.{Elem, NodeSeq}

abstract class F7MultiSelectFieldBase[T]()(implicit renderer: MultiSelectF7FieldRenderer) extends StandardF7Field
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
  with F7FieldWithLabel
  with F7FieldWithAdditionalAttrs
  with F7FieldWithDependencies
  with F7FieldWithValue[Set[T]]
  with F7FieldWithOptionsNsLabel[T] {

  override def loadFromString(str: String): Seq[(F7Field, NodeSeq)] = {
    val all = options()
    val id2Option: Map[String, T] = all.map(opt => _option2Id(opt, all) -> opt).toMap
    val selected: Seq[T] = str.split(";").toList.flatMap(id => {
      id2Option.get(id)
    })
    currentValue = selected.toSet
    _setter(currentValue)
    Nil
  }

  override def saveToString(): Option[String] = Some(currentValue.map(opt => _option2Id(opt, options())).mkString(";"))

  override def submit()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = super.submit() & _setter(currentValue)

  def focusJs: Js = Js.focus(elemId) & Js.select(elemId)

  def finalAdditionalAttrs: Seq[(String, String)] = additionalAttrs

  override def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    val renderedOptions = options()
    val ids2Option: Map[String, T] = renderedOptions.map(opt => fsc.session.nextID() -> opt).toMap
    val option2Id: Map[T, String] = ids2Option.map(_.swap)
    val optionsRendered = renderedOptions.map(opt => {
      renderer.renderOption(this)(currentValue.contains(opt), option2Id(opt), _option2NodeSeq(opt))
    })

    val errorsAtRenderTime = validate()

    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { hints =>
        val onchangeJs = fsc.callback(Js.selectedValues(Js.elementById(elemId)), {
          case ids =>
            currentValue = ids.split(",").filter(_.trim != "").toSet[String].map(id => ids2Option(id))
            form.onEvent(ChangedField(this)(hints)) &
              (if (hints.contains(ShowValidationsHint) || errorsAtRenderTime.nonEmpty || validate().nonEmpty) reRender()(form, fsc, hints) & Js.focus(elemId) else Js.void)
        }).cmd
        renderer.render(this)(
          label.map(label => <label for={elemId}>{label}</label>),
          processInputElem(
            <select
              multiple="multiple"
              name={name.getOrElse(null)}
              onblur={onchangeJs}
              onchange={onchangeJs}
              id={elemId}
            >{optionsRendered}</select>),
          errorsAtRenderTime.headOption.map(_._2)
        )(hints)
      }
    }
  }

  override def fieldAndChildreenMatchingPredicate(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] = if (predicate.applyOrElse[F7Field, Boolean](this, _ => false)) List(this) else Nil
}
