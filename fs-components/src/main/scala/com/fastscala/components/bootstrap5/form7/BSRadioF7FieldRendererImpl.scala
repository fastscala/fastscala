package com.fastscala.components.bootstrap5.form7

import com.fastscala.components.bootstrap5.form7.renderermodifiers.{CheckboxAlignment, CheckboxSide, CheckboxStyle}
import com.fastscala.components.form7.fields.radio.F7RadioFieldBase
import com.fastscala.components.form7.renderers.RadioF7FieldRenderer
import com.fastscala.components.utils.Mutable
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromNodeSeq
import com.fastscala.scala_xml.js.JS
import com.fastscala.utils.IdGen

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

abstract class BSRadioF7FieldRendererImpl()(
  implicit
  checkboxAlignment: CheckboxAlignment.Value,
  checkboxStyle: CheckboxStyle.Value,
  checkboxSide: CheckboxSide.Value,
) extends RadioF7FieldRenderer with Mutable {

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

  override def render(field: F7RadioFieldBase[?])(
    inputElemsAndLabels: Seq[(Elem, Option[Elem])],
    label: Option[Elem],
    invalidFeedback: Option[Elem],
    validFeedback: Option[Elem],
    help: Option[Elem]
  ): Elem = {
    div.mb_3.withId(field.aroundId).apply {
      val labelId = label.flatMap(_.getIdOpt).getOrElse(field.labelId)
      val invalidFeedbackId = invalidFeedback.flatMap(_.getIdOpt).getOrElse(field.invalidFeedbackId)
      val validFeedbackId = validFeedback.flatMap(_.getIdOpt).getOrElse(field.validFeedbackId)
      val helpId = help.flatMap(_.getIdOpt).getOrElse(field.helpId)

      label.map(_.withIdIfNotSet(labelId).form_label.pipe(onLabelTransforms).me_2).getOrElse(Empty) ++
        inputElemsAndLabels.map({
          case (inputElem, label) =>
            form_check
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
              .pipe(elem => {
                if (invalidFeedback.isDefined) elem.is_invalid
                else if (validFeedback.isDefined) elem.is_valid
                else elem
              }).apply {
                val inputId = inputElem.getIdOpt.getOrElse(IdGen.id("input"))
                val labelId = label.flatMap(_.getIdOpt).getOrElse(IdGen.id("label"))
                inputElem.withName(field.radioNameId).withIdIfNotSet(inputId).form_check_input.pipe(elem => {
                  if (invalidFeedback.isDefined) elem.is_invalid
                  else if (validFeedback.isDefined) elem.is_valid
                  else elem
                }.withAttrs(
                  (if (invalidFeedback.isEmpty && validFeedback.isEmpty) {
                    help.map(help => "aria-describedby" -> help.getIdOpt.getOrElse(field.helpId)).toSeq
                  } else {
                    invalidFeedback.map(invalidFeedback => "aria-describedby" -> invalidFeedback.getIdOpt.getOrElse(field.invalidFeedbackId)).toSeq
                  }) ++
                    label.map(help => "aria-labelledby" -> labelId) *
                ).pipe(onInputElemTransforms) ++
                  label.map(_.form_check_label.withIdIfNotSet(labelId).withFor(inputId).pipe(onLabelTransforms)).getOrElse(Empty): NodeSeq)
              }
        }).mkNS ++
        invalidFeedback.getOrElse(div.visually_hidden).invalid_feedback.withIdIfNotSet(invalidFeedbackId).pipe(onInvalidFeedbackTransforms) ++
        validFeedback.getOrElse(div.visually_hidden).valid_feedback.withIdIfNotSet(validFeedbackId).pipe(onValidFeedbackTransforms) ++
        help.getOrElse(div.visually_hidden).form_text.withIdIfNotSet(helpId).pipe(onHelpTransforms)
    }.pipe(onAroundDivTransforms)
  }

  def showOrUpdateValidation(
                              field: F7RadioFieldBase[?]
                            )(ns: NodeSeq): Js =
    JS.setContents(field.invalidFeedbackId, ns) &
      JS.removeClass(field.invalidFeedbackId, "visually-hidden") &
      JS.addClass(field.validFeedbackId, "visually-hidden") &
      JS.addClassToElemsMatchingSelector(s"#${field.aroundId} > .form-check", "is-invalid") &
      JS.addClassToElemsMatchingSelector(s"#${field.aroundId} > .form-check > input", "is-invalid") &
      JS.removeClassFromElemsMatchingSelector(s"#${field.aroundId} > .form-check", "is-valid") &
      JS.removeClassFromElemsMatchingSelector(s"#${field.aroundId} > .form-check > input", "is-valid")

  def hideValidation(
                      field: F7RadioFieldBase[?]
                    )(): Js =
    JS.addClass(field.invalidFeedbackId, "visually-hidden") &
      JS.removeClassFromElemsMatchingSelector(s"#${field.aroundId} > .form-check", "is-invalid") &
      JS.removeClassFromElemsMatchingSelector(s"#${field.aroundId} > .form-check > input", "is-invalid")
}
