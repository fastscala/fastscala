package com.fastscala.templates.bootstrap5.form7

import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.helpers.BSHelpers
import com.fastscala.templates.form7._
import com.fastscala.templates.form7.fields._
import com.fastscala.templates.form7.fields.select.{F7MultiSelectFieldBase, F7SelectFieldBase}
import com.fastscala.templates.form7.fields.text.{F7TextField, F7TextareaField}
import com.fastscala.templates.form7.renderers._
import com.fastscala.xml.scala_xml.JS
import com.fastscala.xml.scala_xml.JS.RichJs

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

abstract class BSForm7Renderer {

  import com.fastscala.templates.bootstrap5.helpers.BSHelpers._

  def defaultRequiredFieldLabel: String

  implicit val bsFormRenderer: BSForm7Renderer = this

  //  implicit val dateFieldOptRenderer = new DateFieldOptRenderer {
  //    override def defaultRequiredFieldLabel: String = BSForm7Renderer.this.defaultRequiredFieldLabel
  //  }

  implicit def textFieldRenderer: TextF7FieldRenderer = new TextF7FieldRenderer {

    def defaultRequiredFieldLabel: String = BSForm7Renderer.this.defaultRequiredFieldLabel

    override def render(
                         field: F7TextField[_],
                       )(
                         inputElem: Elem,
                         label: Option[Elem],
                         invalidFeedback: Option[Elem],
                         validFeedback: Option[Elem],
                         help: Option[Elem],
                       ): Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle("display:none;")
      else {
        div.mb_3.withId(field.aroundId).apply {
          (label.map(_.form_label.withFor(field.elemId)).getOrElse(Empty) ++
            inputElem.form_control.pipe(elem => {
              if (invalidFeedback.isDefined) elem.is_invalid
              else if (validFeedback.isDefined) elem.is_valid
              else elem
            }).withAttrs(
              (if (invalidFeedback.isEmpty && validFeedback.isEmpty) {
                help.map(help => "aria-describedby" -> help.getId.getOrElse(field.elemId + "-help")).toSeq
              } else {
                invalidFeedback.map(invalidFeedback => "aria-describedby" -> invalidFeedback.getId.getOrElse(field.elemId + "-invalid-feedback")).toSeq
              }): _*
            ) ++
            invalidFeedback.getOrElse(div.visually_hidden).invalid_feedback.withFor(field.elemId).withIdIfNotSet(field.elemId + "-invalid-feedback") ++
            validFeedback.getOrElse(div.visually_hidden).valid_feedback.withIdIfNotSet(field.elemId + "-valid-feedback") ++
            help.filter(_ => invalidFeedback.isEmpty && validFeedback.isEmpty).getOrElse(div.visually_hidden).form_text.withIdIfNotSet(field.elemId + "-help")
            : NodeSeq)
        }
      }
    }

    override def showValidation(
                                 field: F7TextField[_]
                               )(ns: NodeSeq): Js = (
      JS.setContents(field.elemId + "-invalid-feedback", ns) &
        JS.removeClass(field.elemId + "-invalid-feedback", "visually-hidden") &
        JS.addClass(field.elemId + "-valid-feedback", "visually-hidden") &
        JS.addClass(field.elemId, "is-invalid") &
        JS.removeClass(field.elemId, "is-valid") &
        JS.setAttr(field.elemId)("aria-describedby", field.elemId + "-invalid-feedback")
      ).printBeforeExec

    override def hideValidation(
                                 field: F7TextField[_]
                               )(): Js =
      JS.addClass(field.elemId + "-invalid-feedback", "visually-hidden") &
        JS.removeClass(field.elemId, "is-invalid") &
        JS.removeAttr(field.elemId, "aria-describedby")
  }

  def textareaFieldRendererTextareaElemClasses: String = form_control.getClassAttr

  def textareaFieldRendererTextareaElemStyle: String = form_control.getStyleAttr

  implicit def textareaFieldRenderer: TextareaF7FieldRenderer = new TextareaF7FieldRenderer {

    def defaultRequiredFieldLabel: String = BSForm7Renderer.this.defaultRequiredFieldLabel

    override def render[T](
                            field: F7TextareaField[T]
                          )(
                            label: Option[NodeSeq],
                            inputElem: Elem,
                            error: Option[NodeSeq]
                          )(implicit hints: Seq[RenderHint]): Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.withId(field.aroundId).apply {
        val showErrors = hints.contains(ShowValidationsHint)
        label.map(lbl => BSHelpers.label.form_label.withAttr("for" -> field.elemId)(lbl)).getOrElse(Empty) ++
          inputElem
            .withClass(textareaFieldRendererTextareaElemClasses)
            .withStyle(textareaFieldRendererTextareaElemStyle)
            .withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr)
            .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
            .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true") ++
          error.filter(_ => showErrors).map(error => invalid_feedback.apply(error)).getOrElse(Empty)
      }
    }
  }

  def selectFieldRendererSelectElemClasses: String = form_select.form_control.getClassAttr

  implicit def selectFieldRenderer: SelectF7FieldRenderer = new SelectF7FieldRenderer {

    def defaultRequiredFieldLabel: String = BSForm7Renderer.this.defaultRequiredFieldLabel

    override def render[T](
                            field: F7SelectFieldBase[T]
                          )(
                            label: Option[Elem],
                            elem: Elem,
                            error: Option[NodeSeq]
                          )(implicit hints: Seq[RenderHint]): Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.withId(field.aroundId).apply {
        val showErrors = true // hints.contains(ShowValidationsHint)
        label.map(lbl => BSHelpers.label.form_label.withAttr("for" -> field.elemId)(lbl)).getOrElse(Empty) ++
          elem
            .addClass(selectFieldRendererSelectElemClasses)
            .withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr)
            .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
            .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true") ++
          error.filter(_ => showErrors).map(error => invalid_feedback.apply(error)).getOrElse(Empty)
      }
    }

    override def renderOption[T](field: F7SelectFieldBase[T])(selected: Boolean, value: String, label: NodeSeq)(implicit hints: Seq[RenderHint]): Elem =
      <option selected={if (selected) "true" else null} value={value}>{label}</option>
  }

  def multiSelectFieldRendererSelectElemClasses: String = form_select.form_control.getClassAttr

  implicit def multiSelectFieldRenderer: MultiSelectF7FieldRenderer = new MultiSelectF7FieldRenderer {

    def defaultRequiredFieldLabel: String = BSForm7Renderer.this.defaultRequiredFieldLabel

    override def render[T](field: F7MultiSelectFieldBase[T])(label: Option[Elem], elem: Elem, error: Option[NodeSeq])(implicit hints: Seq[RenderHint]): Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.withId(field.aroundId).apply {
        val showErrors = true // hints.contains(ShowValidationsHint)
        label.map(lbl => BSHelpers.label.form_label.withAttr("for" -> field.elemId)(lbl)).getOrElse(Empty) ++
          elem
            .addClass(selectFieldRendererSelectElemClasses)
            .withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr)
            .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
            .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true") ++
          error.filter(_ => showErrors).map(error => invalid_feedback.apply(error)).getOrElse(Empty)
      }
    }

    override def renderOption[T](field: F7MultiSelectFieldBase[T])(selected: Boolean, value: String, label: NodeSeq)(implicit hints: Seq[RenderHint]): Elem = {
      println(<option selected={if (selected) "true" else null} value={value}>{label}</option>)
      <option selected={if (selected) "true" else null} value={value}>{label}</option>
    }
  }

  def checkboxFieldRendererCheckboxElemClasses: String = form_check_input.getClassAttr

  implicit def checkboxFieldRenderer: CheckboxF7FieldRenderer = new CheckboxF7FieldRenderer {

    override def render(
                         field: F7CheckboxField
                       )(
                         label: Option[Elem],
                         elem: Elem,
                         error: Option[NodeSeq]
                       )(implicit hints: Seq[RenderHint]): Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.form_check.withId(field.aroundId).apply {
        val showErrors = hints.contains(ShowValidationsHint)
        elem
          .addClass(checkboxFieldRendererCheckboxElemClasses)
          .withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr)
          .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
          .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true") ++
          label.map(lbl => BSHelpers.label.form_check_label.withFor(field.elemId)(lbl)).getOrElse(Empty) ++
          error.filter(_ => showErrors).map(error => invalid_feedback.apply(error)).getOrElse(Empty)
      }
    }
  }

  //  implicit val fileUploadFieldRenderer = new FileUploadFieldRenderer {
  //    override def transformFormElem(field: F5FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = super.transformFormElem(field)(elem).mb_3
  //  }

  implicit def buttonFieldRenderer: ButtonF7FieldRenderer = new ButtonF7FieldRenderer {
    override def render(field: F7SaveButtonField[_])(btn: Elem)(implicit hints: Seq[RenderHint]): Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.addClass("d-grid gap-2 d-md-flex justify-content-md-end").withId(field.aroundId)(
        btn
          .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
          .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true")
      )
    }
  }

  implicit def formRenderer: F7FormRenderer = new F7FormRenderer {
    override def render(form: Elem): Elem = form.mb_5.w_100.addClass("form")
  }
}
