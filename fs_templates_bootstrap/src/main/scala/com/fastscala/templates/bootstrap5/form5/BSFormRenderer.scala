package com.fastscala.templates.bootstrap5.form5

import com.fastscala.templates.bootstrap5.classes.BSHelpers
import com.fastscala.templates.form5.FormRenderer
import com.fastscala.templates.form5.fields._
import com.fastscala.templates.utils.Button
import com.fastscala.utils.NodeSeqUtils

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

abstract class BSFormRenderer {

  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

  def defaultRequiredFieldLabel: String

  implicit val bsFormRenderer: BSFormRenderer = this

  implicit val dateFieldOptRenderer = new DateFieldOptRenderer {
    override def defaultRequiredFieldLabel: String = BSFormRenderer.this.defaultRequiredFieldLabel
  }

  implicit val textFieldRenderer = new TextFieldRenderer {

    def defaultRequiredFieldLabel: String = BSFormRenderer.this.defaultRequiredFieldLabel

    override def render[T](
                            field: TextField[T]
                          )(
                            label: Option[NodeSeq],
                            inputElem: Elem,
                            error: Option[NodeSeq]
                          )(implicit hints: Seq[RenderHint]): Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.withId(field.aroundId) {
        val showErrors = hints.contains(ShowValidationsHint)
        label.map(lbl => <label/>.form_label.withAttr("for" -> field.elemId)(lbl)).getOrElse(NodeSeq.Empty) ++
          inputElem.form_control.withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr)
            .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
            .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true") ++
          error.filter(_ => showErrors).map(error => invalid_feedback(error)).getOrElse(NodeSeq.Empty)
      }
    }
  }

  implicit val textareaFieldRenderer = new TextareaFieldRenderer {

    def defaultRequiredFieldLabel: String = BSFormRenderer.this.defaultRequiredFieldLabel

    override def render(
                         field: TextAreaField
                       )(
                         label: Option[NodeSeq],
                         inputElem: Elem,
                         error: Option[NodeSeq]
                       )(implicit hints: Seq[RenderHint]): Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.withId(field.aroundId) {
        val showErrors = hints.contains(ShowValidationsHint)
        label.map(lbl => <label/>.form_label.withAttr("for" -> field.elemId)(lbl)).getOrElse(NodeSeq.Empty) ++
          inputElem.form_control.withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr)
            .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
            .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true") ++
          error.filter(_ => showErrors).map(error => invalid_feedback(error)).getOrElse(NodeSeq.Empty)
      }
    }
  }

  implicit val selectFieldRenderer = new SelectFieldRenderer {

    def defaultRequiredFieldLabel: String = BSFormRenderer.this.defaultRequiredFieldLabel

    override def render[T](
                            field: SelectField[T]
                          )(
                            label: Option[Elem],
                            elem: Elem,
                            error: Option[NodeSeq]
                          )(implicit hints: Seq[RenderHint]): Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.withId(field.aroundId) {
        val showErrors = true // hints.contains(ShowValidationsHint)
        label.map(lbl => <label/>.form_label.withAttr("for" -> field.elemId)(lbl)).getOrElse(NodeSeq.Empty) ++
          elem.addClass("form-select form-control").withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr)
            .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
            .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true") ++
          error.filter(_ => showErrors).map(error => invalid_feedback(error)).getOrElse(NodeSeq.Empty)
      }
    }
  }

  implicit val multiSelectFieldRenderer = new MultiSelectFieldRenderer {

    def defaultRequiredFieldLabel: String = BSFormRenderer.this.defaultRequiredFieldLabel

    override def render[T](
                            field: MultiSelectField[T]
                          )(
                            label: Option[Elem],
                            elem: Elem,
                            error: Option[NodeSeq]
                          )(implicit hints: Seq[RenderHint]): Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.withId(field.aroundId) {
        val showErrors = true // hints.contains(ShowValidationsHint)
        label.map(lbl => <label/>.form_label.withAttr("for" -> field.elemId)(lbl)).getOrElse(NodeSeq.Empty) ++
          elem.addClass("form-select form-control").withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr)
            .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
            .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true") ++
          error.filter(_ => showErrors).map(error => invalid_feedback(error)).getOrElse(NodeSeq.Empty)
      }
    }
  }

  implicit val checkboxFieldRenderer = new CheckboxFieldRenderer {

    override def render(
                         field: CheckboxField
                       )(
                         label: Option[Elem],
                         elem: Elem,
                         error: Option[NodeSeq]
                       )(implicit hints: Seq[RenderHint]): Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.form_check.withId(field.aroundId) {
        val showErrors = hints.contains(ShowValidationsHint)
        elem.addClass("form-check-input").withClassIf(showErrors && error.isDefined, is_invalid.getClassAttr)
          .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
          .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true") ++
          label.map(lbl => <label/>.form_check_label.withFor(field.elemId)(lbl)).getOrElse(NodeSeq.Empty) ++
          error.filter(_ => showErrors).map(error => invalid_feedback(error)).getOrElse(NodeSeq.Empty)
      }
    }
  }

  implicit val fileUploadFieldRenderer = new FileUploadFieldRenderer {
    override def transformFormElem(field: FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = super.transformFormElem(field)(elem).mb_3
  }

  implicit val buttonFieldRenderer = new ButtonFieldRenderer {
    override def render(field: SaveButtonField[_])(btn: Elem)(implicit hints: Seq[RenderHint]): Elem = {
      if (!field.enabled()) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.addClass("d-grid gap-2 d-md-flex justify-content-md-end").withId(field.aroundId)(
        btn
          .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
          .withAttrIf(hints.contains(ReadOnlyFieldsHint), "readonly" -> "true")
      )
    }
  }

  implicit val formRenderer: FormRenderer = new FormRenderer {
    override def render(form: Elem): Elem =
      form.mb_5.w_100.addClass("form")
  }
}
