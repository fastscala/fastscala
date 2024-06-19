package com.fastscala.templates.bootstrap5.form6

import com.fastscala.templates.form6.F6FormRenderer
import com.fastscala.templates.form6.fields._

import scala.xml.{Elem, NodeSeq}

abstract class BSForm6Renderer {

  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

  def defaultRequiredFieldLabel: String

  implicit val bsFormRenderer: BSForm6Renderer = this

  //  implicit val dateFieldOptRenderer = new DateFieldOptRenderer {
  //    override def defaultRequiredFieldLabel: String = BSForm6Renderer.this.defaultRequiredFieldLabel
  //  }

  def textFieldRendererInputElemClasses: String = form_control.getClassAttr

  def textFieldRendererInputElemStyle: String = form_control.getStyleAttr

  implicit val textFieldRenderer = new TextF6FieldRenderer {

    def defaultRequiredFieldLabel: String = BSForm6Renderer.this.defaultRequiredFieldLabel

    override def render[T](
                            field: F6TextField[T]
                          )(
                            label: Option[NodeSeq],
                            inputElem: Elem,
                            error: Option[NodeSeq]
                          )(implicit hints: Seq[RenderHint]): Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.withId(field.aroundId).apply {
        val showErrors = hints.contains(ShowValidationsHint)
        label.map(lbl => <label/>.form_label.withAttr("for" -> field.elemId)(lbl)).getOrElse(NodeSeq.Empty) ++
          inputElem
            .withClass(textFieldRendererInputElemClasses)
            .withStyle(textFieldRendererInputElemStyle)
            .withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr)
            .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
            .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true") ++
          error.filter(_ => showErrors).map(error => invalid_feedback.apply(error)).getOrElse(NodeSeq.Empty)
      }
    }
  }

  def textareaFieldRendererTextareaElemClasses: String = form_control.getClassAttr

  def textareaFieldRendererTextareaElemStyle: String = form_control.getStyleAttr

  implicit val textareaFieldRenderer = new TextareaF6FieldRenderer {

    def defaultRequiredFieldLabel: String = BSForm6Renderer.this.defaultRequiredFieldLabel

    override def render[T](
                            field: F6TextareaField[T]
                          )(
                            label: Option[NodeSeq],
                            inputElem: Elem,
                            error: Option[NodeSeq]
                          )(implicit hints: Seq[RenderHint]): Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.withId(field.aroundId).apply {
        val showErrors = hints.contains(ShowValidationsHint)
        label.map(lbl => <label/>.form_label.withAttr("for" -> field.elemId)(lbl)).getOrElse(NodeSeq.Empty) ++
          inputElem
            .withClass(textareaFieldRendererTextareaElemClasses)
            .withStyle(textareaFieldRendererTextareaElemStyle)
            .withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr)
            .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
            .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true") ++
          error.filter(_ => showErrors).map(error => invalid_feedback.apply(error)).getOrElse(NodeSeq.Empty)
      }
    }
  }

  def selectFieldRendererSelectElemClasses: String = form_select.form_control.getClassAttr

  implicit val selectFieldRenderer = new SelectF6FieldRenderer {

    def defaultRequiredFieldLabel: String = BSForm6Renderer.this.defaultRequiredFieldLabel

    override def render[T](
                            field: F6SelectFieldBase[T]
                          )(
                            label: Option[Elem],
                            elem: Elem,
                            error: Option[NodeSeq]
                          )(implicit hints: Seq[RenderHint]): Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.withId(field.aroundId).apply {
        val showErrors = true // hints.contains(ShowValidationsHint)
        label.map(lbl => <label/>.form_label.withAttr("for" -> field.elemId)(lbl)).getOrElse(NodeSeq.Empty) ++
          elem
            .addClass(selectFieldRendererSelectElemClasses)
            .withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr)
            .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
            .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true") ++
          error.filter(_ => showErrors).map(error => invalid_feedback.apply(error)).getOrElse(NodeSeq.Empty)
      }
    }

    override def renderOption[T](field: F6SelectFieldBase[T])(selected: Boolean, value: String, label: NodeSeq)(implicit hints: Seq[RenderHint]): Elem =
      <option selected={if (selected) "true" else null} value={value}>{label}</option>
  }

  def multiSelectFieldRendererSelectElemClasses: String = form_select.form_control.getClassAttr

  //  implicit val multiSelectFieldRenderer = new MultiSelectFieldRenderer {
  //
  //    def defaultRequiredFieldLabel: String = BSForm6Renderer.this.defaultRequiredFieldLabel
  //
  //    override def render[T](
  //                            field: F5MultiSelectField[T]
  //                          )(
  //                            label: Option[Elem],
  //                            elem: Elem,
  //                            error: Option[NodeSeq]
  //                          )(implicit hints: Seq[RenderHint]): Elem = {
  //      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
  //      else div.mb_3.withId(field.aroundId) {
  //        val showErrors = true // hints.contains(ShowValidationsHint)
  //        label.map(lbl => <label/>.form_label.withAttr("for" -> field.elemId)(lbl)).getOrElse(NodeSeq.Empty) ++
  //          elem
  //            .addClass(multiSelectFieldRendererSelectElemClasses)
  //            .withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr)
  //            .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
  //            .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true") ++
  //          error.filter(_ => showErrors).map(error => invalid_feedback(error)).getOrElse(NodeSeq.Empty)
  //      }
  //    }
  //  }

  def checkboxFieldRendererCheckboxElemClasses: String = form_check_input.getClassAttr

  implicit val checkboxFieldRenderer = new CheckboxF6FieldRenderer {

    override def render(
                         field: F6CheckboxField
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
          label.map(lbl => <label/>.form_check_label.withFor(field.elemId)(lbl)).getOrElse(NodeSeq.Empty) ++
          error.filter(_ => showErrors).map(error => invalid_feedback.apply(error)).getOrElse(NodeSeq.Empty)
      }
    }
  }

//  implicit val fileUploadFieldRenderer = new FileUploadFieldRenderer {
//    override def transformFormElem(field: F5FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = super.transformFormElem(field)(elem).mb_3
//  }

  implicit val buttonFieldRenderer = new ButtonF6FieldRenderer {
    override def render(field: F6SaveButtonField[_])(btn: Elem)(implicit hints: Seq[RenderHint]): Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.addClass("d-grid gap-2 d-md-flex justify-content-md-end").withId(field.aroundId)(
        btn
          .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
          .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true")
      )
    }
  }

  implicit val formRenderer: F6FormRenderer = new F6FormRenderer {
    override def render(form: Elem): Elem =
      form.mb_5.w_100.addClass("form")
  }
}
