package com.fastscala.templates.bootstrap5.form7

import com.fastscala.templates.form7.mixins.StandardF7Field
import com.fastscala.templates.form7.renderers.CheckboxF7FieldRenderer

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

trait BSCheckboxF7FieldRendererImpl extends CheckboxF7FieldRenderer with BSStandardF7FieldRendererImpl {

  import com.fastscala.templates.bootstrap5.helpers.BSHelpers._

  override def render(
                       field: StandardF7Field,
                     )(
                       inputElem: Elem,
                       label: Option[Elem],
                       invalidFeedback: Option[Elem],
                       validFeedback: Option[Elem],
                       help: Option[Elem],
                     ): Elem = {
    div.form_check.mb_3.withId(field.aroundId).apply {
      (inputElem.withIdIfNotSet(field.elemId).form_check_input.pipe(elem => {
          if (invalidFeedback.isDefined) elem.is_invalid
          else if (validFeedback.isDefined) elem.is_valid
          else elem
        }).withAttrs(
          (if (invalidFeedback.isEmpty && validFeedback.isEmpty) {
            help.map(help => "aria-describedby" -> help.getId.getOrElse(field.helpId)).toSeq
          } else {
            invalidFeedback.map(invalidFeedback => "aria-describedby" -> invalidFeedback.getId.getOrElse(field.invalidFeedbackId)).toSeq
          }): _*
        ) ++
        label.map(_.form_check_label.withFor(field.elemId)).getOrElse(Empty) ++
        invalidFeedback.getOrElse(div.visually_hidden).invalid_feedback.withFor(field.elemId).withIdIfNotSet(field.invalidFeedbackId) ++
        validFeedback.getOrElse(div.visually_hidden).valid_feedback.withIdIfNotSet(field.validFeedbackId) ++
        help.getOrElse(div.visually_hidden).form_text.withIdIfNotSet(field.helpId)
        : NodeSeq)
    }
  }
}
