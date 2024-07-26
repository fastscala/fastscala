package com.fastscala.templates.bootstrap5.form5

import com.fastscala.core.{FSXmlEnv, FSXmlSupport}
import com.fastscala.templates.bootstrap5.classes.BSHelpers
import com.fastscala.templates.form5.FormRenderer
import com.fastscala.templates.form5.fields._

abstract class BSFormRenderer {

  import com.fastscala.core.FSXmlUtils._
  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

  def defaultRequiredFieldLabel: String

  implicit val bsFormRenderer: BSFormRenderer = this

  implicit val dateFieldOptRenderer: DateFieldOptRenderer = new DateFieldOptRenderer {
    override def defaultRequiredFieldLabel: String = BSFormRenderer.this.defaultRequiredFieldLabel
  }

  def textFieldRendererInputElemClasses[E <: FSXmlEnv](implicit fsXmlSupport: FSXmlSupport[E]): String = form_control.getClassAttr

  def textFieldRendererInputElemStyle[E <: FSXmlEnv](implicit fsXmlSupport: FSXmlSupport[E]): String = form_control.getStyleAttr

  implicit val textFieldRenderer: TextFieldRenderer = new TextFieldRenderer {

    def defaultRequiredFieldLabel: String = BSFormRenderer.this.defaultRequiredFieldLabel

    override def render[E <: FSXmlEnv : FSXmlSupport, T](
                                                          field: F5TextField[E, T]
                                                        )(
                                                          label: Option[E#NodeSeq],
                                                          inputElem: E#Elem,
                                                          error: Option[E#NodeSeq]
                                                        )(implicit hints: Seq[RenderHint]): E#Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.withId(field.aroundId).apply {
        val showErrors = hints.contains(ShowValidationsHint)
        label.map(lbl => BSHelpers.label.form_label.withAttr("for" -> field.elemId).apply(lbl)).getOrElse(implicitly[FSXmlSupport[E]].Empty) ++
          inputElem
            .withClass(textFieldRendererInputElemClasses)
            .withStyle(textFieldRendererInputElemStyle)
            .withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr)
            .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
            .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true") ++
          error.filter(_ => showErrors).map(error => invalid_feedback.apply(error)).getOrElse(implicitly[FSXmlSupport[E]].Empty)
      }
    }
  }

  def textareaFieldRendererTextareaElemClasses[E <: FSXmlEnv](implicit fsXmlSupport: FSXmlSupport[E]): String = form_control.getClassAttr

  def textareaFieldRendererTextareaElemStyle[E <: FSXmlEnv](implicit fsXmlSupport: FSXmlSupport[E]): String = form_control.getStyleAttr

  implicit val textareaFieldRenderer: TextareaFieldRenderer = new TextareaFieldRenderer {

    def defaultRequiredFieldLabel: String = BSFormRenderer.this.defaultRequiredFieldLabel

    override def render[E <: FSXmlEnv : FSXmlSupport](
                                                       field: F5TextAreaField[E]
                                                     )(
                                                       label: Option[E#NodeSeq],
                                                       inputElem: E#Elem,
                                                       error: Option[E#NodeSeq]
                                                     )(implicit hints: Seq[RenderHint]): E#Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.withId(field.aroundId).apply {
        val showErrors = hints.contains(ShowValidationsHint)
        label.map(lbl => BSHelpers.label.form_label.withAttr("for" -> field.elemId)(lbl)).getOrElse(implicitly[FSXmlSupport[E]].Empty) ++
          inputElem
            .withClass(textareaFieldRendererTextareaElemClasses)
            .withStyle(textareaFieldRendererTextareaElemStyle)
            .withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr)
            .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
            .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true") ++
          error.filter(_ => showErrors).map(error => invalid_feedback.apply(error)).getOrElse(implicitly[FSXmlSupport[E]].Empty)
      }
    }
  }

  def selectFieldRendererSelectElemClasses[E <: FSXmlEnv](implicit fsXmlSupport: FSXmlSupport[E]): String = form_select.form_control.getClassAttr

  implicit val selectFieldRenderer: SelectFieldRenderer = new SelectFieldRenderer {

    def defaultRequiredFieldLabel: String = BSFormRenderer.this.defaultRequiredFieldLabel

    override def render[E <: FSXmlEnv : FSXmlSupport, T](
                                                          field: F5SelectField[E, T]
                                                        )(
                                                          label: Option[E#Elem],
                                                          elem: E#Elem,
                                                          error: Option[E#NodeSeq]
                                                        )(implicit hints: Seq[RenderHint]): E#Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.withId(field.aroundId).apply {
        val showErrors = true // hints.contains(ShowValidationsHint)
        label.map(lbl => BSHelpers.label.form_label.withAttr("for" -> field.elemId)(lbl)).getOrElse(implicitly[FSXmlSupport[E]].Empty) ++
          elem
            .addClass(selectFieldRendererSelectElemClasses)
            .withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr)
            .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
            .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true") ++
          error.filter(_ => showErrors).map(error => invalid_feedback.apply(error)).getOrElse(implicitly[FSXmlSupport[E]].Empty)
      }
    }
  }

  def multiSelectFieldRendererSelectElemClasses[E <: FSXmlEnv](implicit fsXmlSupport: FSXmlSupport[E]): String = form_select.form_control.getClassAttr

  implicit val multiSelectFieldRenderer: MultiSelectFieldRenderer = new MultiSelectFieldRenderer {

    def defaultRequiredFieldLabel: String = BSFormRenderer.this.defaultRequiredFieldLabel

    override def render[E <: FSXmlEnv : FSXmlSupport, T](
                                                          field: F5MultiSelectField[E, T]
                                                        )(
                                                          label: Option[E#Elem],
                                                          elem: E#Elem,
                                                          error: Option[E#NodeSeq]
                                                        )(implicit hints: Seq[RenderHint]): E#Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.withId(field.aroundId).apply {
        val showErrors = true // hints.contains(ShowValidationsHint)
        label.map(lbl => BSHelpers.label.form_label.withAttr("for" -> field.elemId)(lbl)).getOrElse(implicitly[FSXmlSupport[E]].Empty) ++
          elem
            .addClass(multiSelectFieldRendererSelectElemClasses)
            .withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr)
            .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
            .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true") ++
          error.filter(_ => showErrors).map(error => invalid_feedback.apply(error)).getOrElse(implicitly[FSXmlSupport[E]].Empty)
      }
    }
  }

  def checkboxFieldRendererCheckboxElemClasses[E <: FSXmlEnv](implicit fsXmlSupport: FSXmlSupport[E]): String = form_check_input.getClassAttr

  implicit val checkboxFieldRenderer: CheckboxFieldRenderer = new CheckboxFieldRenderer {

    override def render[E <: FSXmlEnv : FSXmlSupport](
                                                       field: F5CheckboxField[E]
                                                     )(
                                                       label: Option[E#Elem],
                                                       elem: E#Elem,
                                                       error: Option[E#NodeSeq]
                                                     )(implicit hints: Seq[RenderHint]): E#Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.form_check.withId(field.aroundId) {
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

  implicit val fileUploadFieldRenderer: FileUploadFieldRenderer = new FileUploadFieldRenderer {
    override def transformFormElem[E <: FSXmlEnv : FSXmlSupport](field: F5FileUploadField[E])(elem: E#Elem)(implicit hints: Seq[RenderHint]): E#Elem = super.transformFormElem(field)(elem).mb_3
  }

  implicit val buttonFieldRenderer: ButtonFieldRenderer = new ButtonFieldRenderer {
    override def render[E <: FSXmlEnv : FSXmlSupport](field: F5SaveButtonField[E, _])(btn: E#Elem)(implicit hints: Seq[RenderHint]): E#Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.addClass("d-grid gap-2 d-md-flex justify-content-md-end").withId(field.aroundId).apply(
        btn
          .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
          .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true")
      )
    }
  }

  implicit def formRenderer[E <: FSXmlEnv : FSXmlSupport]: FormRenderer[E] = (form: E#Elem) => form.mb_5.w_100.addClass("form")
}
