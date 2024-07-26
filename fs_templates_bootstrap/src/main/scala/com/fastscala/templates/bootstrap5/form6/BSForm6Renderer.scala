package com.fastscala.templates.bootstrap5.form6

import com.fastscala.core.{FSXmlEnv, FSXmlSupport}
import com.fastscala.templates.bootstrap5.classes.BSHelpers
import com.fastscala.templates.form6.F6FormRenderer
import com.fastscala.templates.form6.fields._
import com.fastscala.xml.scala_xml.FSScalaXmlSupport.RichElem

import scala.xml.{Elem, NodeSeq}

abstract class BSForm6Renderer {

  import com.fastscala.core.FSXmlUtils._
  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

  def defaultRequiredFieldLabel: String

  implicit val bsFormRenderer: BSForm6Renderer = this

  //  implicit val dateFieldOptRenderer = new DateFieldOptRenderer {
  //    override def defaultRequiredFieldLabel: String = BSForm6Renderer.this.defaultRequiredFieldLabel
  //  }

  def textFieldRendererInputElemClasses[E <: FSXmlEnv : FSXmlSupport]: String = form_control.getClassAttr

  def textFieldRendererInputElemStyle[E <: FSXmlEnv : FSXmlSupport]: String = form_control.getStyleAttr

  implicit def textFieldRenderer[E <: FSXmlEnv : FSXmlSupport]: TextF6FieldRenderer[E] = new TextF6FieldRenderer[E] {

    def defaultRequiredFieldLabel: String = BSForm6Renderer.this.defaultRequiredFieldLabel

    override def render[T](
                            field: F6TextField[E, T]
                          )(
                            label: Option[E#NodeSeq],
                            inputElem: E#Elem,
                            error: Option[E#NodeSeq]
                          )(implicit hints: Seq[RenderHint]): E#Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.withId(field.aroundId).apply {
        val showErrors = hints.contains(ShowValidationsHint)
        label.map(lbl => BSHelpers.label.form_label.withAttr("for" -> field.elemId)(lbl)).getOrElse(Empty) ++
          inputElem
            .withClass(textFieldRendererInputElemClasses)
            .withStyle(textFieldRendererInputElemStyle)
            .withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr)
            .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
            .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true") ++
          error.filter(_ => showErrors).map(error => invalid_feedback.apply(error)).getOrElse(Empty)
      }
    }
  }

  def textareaFieldRendererTextareaElemClasses[E <: FSXmlEnv : FSXmlSupport]: String = form_control.getClassAttr

  def textareaFieldRendererTextareaElemStyle[E <: FSXmlEnv : FSXmlSupport]: String = form_control.getStyleAttr

  implicit def textareaFieldRenderer[E <: FSXmlEnv : FSXmlSupport]: TextareaF6FieldRenderer[E] = new TextareaF6FieldRenderer[E] {

    def defaultRequiredFieldLabel: String = BSForm6Renderer.this.defaultRequiredFieldLabel

    override def render[T](
                            field: F6TextareaField[E, T]
                          )(
                            label: Option[E#NodeSeq],
                            inputElem: E#Elem,
                            error: Option[E#NodeSeq]
                          )(implicit hints: Seq[RenderHint]): E#Elem = {
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

  def selectFieldRendererSelectElemClasses[E <: FSXmlEnv : FSXmlSupport]: String = form_select.form_control.getClassAttr

  implicit def selectFieldRenderer[E <: FSXmlEnv : FSXmlSupport]: SelectF6FieldRenderer[E] = new SelectF6FieldRenderer[E] {

    def defaultRequiredFieldLabel: String = BSForm6Renderer.this.defaultRequiredFieldLabel

    override def render[T](
                            field: F6SelectFieldBase[E, T]
                          )(
                            label: Option[E#Elem],
                            elem: E#Elem,
                            error: Option[E#NodeSeq]
                          )(implicit hints: Seq[RenderHint]): E#Elem = {
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

    override def renderOption[T](field: F6SelectFieldBase[E, T])(selected: Boolean, value: String, label: E#NodeSeq)(implicit hints: Seq[RenderHint]): E#Elem =
      <option selected={if (selected) "true" else null} value={value}>{label}</option>.asFSXml()
  }

  def multiSelectFieldRendererSelectElemClasses[E <: FSXmlEnv : FSXmlSupport]: String = form_select.form_control.getClassAttr

  implicit def multiSelectFieldRenderer[E <: FSXmlEnv : FSXmlSupport]: MultiSelectF6FieldRenderer[E] = new MultiSelectF6FieldRenderer[E] {

    def defaultRequiredFieldLabel: String = BSForm6Renderer.this.defaultRequiredFieldLabel

    override def render[T](field: F6MultiSelectFieldBase[E, T])(label: Option[E#Elem], elem: E#Elem, error: Option[E#NodeSeq])(implicit hints: Seq[RenderHint]): E#Elem = {
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

    override def renderOption[T](field: F6MultiSelectFieldBase[E, T])(selected: Boolean, value: String, label: E#NodeSeq)(implicit hints: Seq[RenderHint]): E#Elem =
      <option selected={if (selected) "true" else null} value={value}>{label}</option>.asFSXml()
  }

  def checkboxFieldRendererCheckboxElemClasses[E <: FSXmlEnv : FSXmlSupport]: String = form_check_input.getClassAttr

  implicit def checkboxFieldRenderer[E <: FSXmlEnv : FSXmlSupport]: CheckboxF6FieldRenderer[E] = new CheckboxF6FieldRenderer[E] {

    override def render(
                         field: F6CheckboxField[E]
                       )(
                         label: Option[E#Elem],
                         elem: E#Elem,
                         error: Option[E#NodeSeq]
                       )(implicit hints: Seq[RenderHint]): E#Elem = {
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

  implicit def buttonFieldRenderer[E <: FSXmlEnv : FSXmlSupport]: ButtonF6FieldRenderer[E] = new ButtonF6FieldRenderer[E] {
    override def render(field: F6SaveButtonField[E, _])(btn: E#Elem)(implicit hints: Seq[RenderHint]): E#Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.addClass("d-grid gap-2 d-md-flex justify-content-md-end").withId(field.aroundId)(
        btn
          .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
          .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true")
      )
    }
  }

  implicit def formRenderer[E <: FSXmlEnv : FSXmlSupport]: F6FormRenderer[E] = new F6FormRenderer[E] {
    override def render(form: E#Elem): E#Elem = form.mb_5.w_100.addClass("form")
  }
}
