package com.fastscala.components.bootstrap5.form7

import com.fastscala.components.form7.mixins.StandardF7Field
import com.fastscala.components.form7.renderers.StandardOneInputElemF7FieldRenderer
import com.fastscala.components.utils.Mutable
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

trait BSStandardF7FieldRendererImpl extends StandardOneInputElemF7FieldRenderer with Mutable {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  protected var onAroundDivTransforms: Elem => Elem = identity[Elem]
  protected var onLabelTransforms: Elem => Elem = identity[Elem]
  protected var onInputElemTransforms: Elem => Elem = identity[Elem]
  protected var onInvalidFeedbackTransforms: Elem => Elem = identity[Elem]
  protected var onValidFeedbackTransforms: Elem => Elem = identity[Elem]
  protected var onHelpTransforms: Elem => Elem = identity[Elem]

  def onAroundDiv(f: Elem => Elem): this.type = mutate {
    onAroundDivTransforms = onAroundDivTransforms andThen f
  }

  def onLabel(f: Elem => Elem): this.type = mutate {
    onLabelTransforms = onLabelTransforms andThen f
  }

  def onInputElem(f: Elem => Elem): this.type = mutate {
    onInputElemTransforms = onInputElemTransforms andThen f
  }

  def onInvalidFeedback(f: Elem => Elem): this.type = mutate {
    onInvalidFeedbackTransforms = onInvalidFeedbackTransforms andThen f
  }

  def onValidFeedback(f: Elem => Elem): this.type = mutate {
    onValidFeedbackTransforms = onValidFeedbackTransforms andThen f
  }

  def onHelp(f: Elem => Elem): this.type = mutate {
    onHelpTransforms = onHelpTransforms andThen f
  }

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
    val inputId = inputElem.getIdOpt.getOrElse(field.elemId)
    val labelId = label.flatMap(_.getIdOpt).getOrElse(field.labelId)
    val invalidFeedbackId = invalidFeedback.flatMap(_.getIdOpt).getOrElse(field.invalidFeedbackId)
    val validFeedbackId = validFeedback.flatMap(_.getIdOpt).getOrElse(field.validFeedbackId)
    val helpId = help.flatMap(_.getIdOpt).getOrElse(field.helpId)

    div.mb_3.withId(aroundId).apply {
      (label.map(_.withIdIfNotSet(labelId).form_label.withFor(inputId).pipe(onLabelTransforms)).getOrElse(Empty) ++
        inputElem.withIdIfNotSet(field.elemId).form_control.pipe(elem => {
          if (invalidFeedback.isDefined) elem.is_invalid
          else if (validFeedback.isDefined) elem.is_valid
          else elem
        }).withAttrs(
          (if (invalidFeedback.isEmpty && validFeedback.isEmpty) {
            help.map(help => "aria-describedby" -> help.getIdOpt.getOrElse(field.helpId)).toSeq
          } else {
            invalidFeedback.map(invalidFeedback => "aria-describedby" -> invalidFeedback.getIdOpt.getOrElse(field.invalidFeedbackId)).toSeq
          }) ++
            label.map(_ => "aria-labelledby" -> helpId).toSeq *
        ).pipe(onInputElemTransforms) ++
        invalidFeedback.getOrElse(div.visually_hidden).invalid_feedback.withFor(field.elemId).withIdIfNotSet(invalidFeedbackId).pipe(onInvalidFeedbackTransforms) ++
        validFeedback.getOrElse(div.visually_hidden).valid_feedback.withIdIfNotSet(validFeedbackId).pipe(onValidFeedbackTransforms) ++
        help.getOrElse(div.visually_hidden).form_text.withIdIfNotSet(helpId).pipe(onHelpTransforms)
        : NodeSeq)
    }.pipe(onAroundDivTransforms)
  }
}
