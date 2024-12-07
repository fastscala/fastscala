package com.fastscala.components.bootstrap5.form7

import com.fastscala.components.form7.mixins.StandardF7Field
import com.fastscala.components.form7.renderers.StandardOneInputElemF7FieldRenderer
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

trait BSStandardF7FieldRendererImpl extends StandardOneInputElemF7FieldRenderer {

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
    val aroundId = field.aroundId
    val inputId = inputElem.getId.getOrElse(field.elemId)
    val labelId = label.flatMap(_.getId).getOrElse(field.labelId)
    val invalidFeedbackId = invalidFeedback.flatMap(_.getId).getOrElse(field.invalidFeedbackId)
    val validFeedbackId = validFeedback.flatMap(_.getId).getOrElse(field.validFeedbackId)
    val helpId = help.flatMap(_.getId).getOrElse(field.helpId)

    div.mb_3.withId(aroundId).apply {
      (label.map(_.withIdIfNotSet(labelId).form_label.withFor(inputId)).getOrElse(Empty) ++
        inputElem.withIdIfNotSet(field.elemId).form_control.pipe(elem => {
          if (invalidFeedback.isDefined) elem.is_invalid
          else if (validFeedback.isDefined) elem.is_valid
          else elem
        }).withAttrs(
          (if (invalidFeedback.isEmpty && validFeedback.isEmpty) {
            help.map(help => "aria-describedby" -> help.getId.getOrElse(field.helpId)).toSeq
          } else {
            invalidFeedback.map(invalidFeedback => "aria-describedby" -> invalidFeedback.getId.getOrElse(field.invalidFeedbackId)).toSeq
          }) ++
            label.map(_ => "aria-labelledby" -> helpId).toSeq: _*
        ) ++
        invalidFeedback.getOrElse(div.visually_hidden).invalid_feedback.withFor(field.elemId).withIdIfNotSet(invalidFeedbackId) ++
        validFeedback.getOrElse(div.visually_hidden).valid_feedback.withIdIfNotSet(validFeedbackId) ++
        help.getOrElse(div.visually_hidden).form_text.withIdIfNotSet(helpId)
        : NodeSeq)
    }
  }
}
