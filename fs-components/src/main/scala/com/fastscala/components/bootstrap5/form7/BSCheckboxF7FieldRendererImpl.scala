package com.fastscala.components.bootstrap5.form7

import com.fastscala.components.bootstrap5.form7.renderermodifiers.{CheckboxAlignment, CheckboxSide, CheckboxStyle}
import com.fastscala.components.form7.mixins.StandardF7Field
import com.fastscala.components.form7.renderers.CheckboxF7FieldRenderer
import com.fastscala.components.utils.Mutable
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

abstract class BSCheckboxF7FieldRendererImpl()(
  implicit
  checkboxAlignment: CheckboxAlignment.Value,
  checkboxStyle: CheckboxStyle.Value,
  checkboxSide: CheckboxSide.Value,
) extends CheckboxF7FieldRenderer with BSStandardF7FieldRendererImpl with Mutable {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  override def render(
                       field: StandardF7Field,
                     )(
                       inputElem: Elem,
                       label: Option[Elem],
                       invalidFeedback: Option[Elem],
                       validFeedback: Option[Elem],
                       help: Option[Elem],
                     ): Elem = {
    div.form_check
      .pipe(elem => checkboxAlignment match {
        case com.fastscala.components.bootstrap5.form7.renderermodifiers.CheckboxAlignment.Vertical => elem
        case com.fastscala.components.bootstrap5.form7.renderermodifiers.CheckboxAlignment.Horizontal => elem.form_check_inline
      })
      .pipe(elem => checkboxStyle match {
        case com.fastscala.components.bootstrap5.form7.renderermodifiers.CheckboxStyle.Checkbox => elem
        case com.fastscala.components.bootstrap5.form7.renderermodifiers.CheckboxStyle.Switch => elem.form_switch
      })
      .pipe(elem => checkboxSide match {
        case com.fastscala.components.bootstrap5.form7.renderermodifiers.CheckboxSide.Normal => elem
        case com.fastscala.components.bootstrap5.form7.renderermodifiers.CheckboxSide.Opposite => elem.form_check_reverse
      })
      .mb_3.withId(field.aroundId).apply {
        (inputElem.withIdIfNotSet(field.elemId).form_check_input.pipe(elem => {
          if (invalidFeedback.isDefined) elem.is_invalid
          else if (validFeedback.isDefined) elem.is_valid
          else elem
        }).withAttrs(
          (if (invalidFeedback.isEmpty && validFeedback.isEmpty) {
            help.map(help => "aria-describedby" -> help.getIdOpt.getOrElse(field.helpId)).toSeq
          } else {
            invalidFeedback.map(invalidFeedback => "aria-describedby" -> invalidFeedback.getIdOpt.getOrElse(field.invalidFeedbackId)).toSeq
          }) ++
            label.map(help => "aria-labelledby" -> help.getIdOpt.getOrElse(field.helpId))*
        ).pipe(onInputElemTransforms) ++
          label.map(_.form_check_label.withFor(field.elemId).pipe(onLabelTransforms)).getOrElse(Empty) ++
          invalidFeedback.getOrElse(div.visually_hidden).invalid_feedback.withFor(field.elemId).withIdIfNotSet(field.invalidFeedbackId).pipe(onInvalidFeedbackTransforms) ++
          validFeedback.getOrElse(div.visually_hidden).valid_feedback.withIdIfNotSet(field.validFeedbackId).pipe(onValidFeedbackTransforms) ++
          help.getOrElse(div.visually_hidden).form_text.withIdIfNotSet(field.helpId).pipe(onHelpTransforms)
          : NodeSeq)
      }.pipe(onAroundDivTransforms)
  }
}
