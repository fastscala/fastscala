package com.fastscala.templates.bootstrap5.form7

import com.fastscala.templates.form7._
import com.fastscala.templates.form7.fields._
import com.fastscala.templates.form7.renderers._

import scala.xml.{Elem, NodeSeq}

abstract class BSForm7Renderers {

  import com.fastscala.templates.bootstrap5.helpers.BSHelpers._

  def defaultRequiredFieldLabel: String

  implicit val bsFormRenderer: BSForm7Renderers = this

  implicit def textFieldRenderer: TextF7FieldRenderer = new TextF7FieldRenderer with BSStandardF7FieldRendererImpl {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel
  }

  implicit def textareaFieldRenderer: TextareaF7FieldRenderer = new TextareaF7FieldRenderer with BSStandardF7FieldRendererImpl {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel
  }

  implicit def selectFieldRenderer: SelectF7FieldRenderer = new SelectF7FieldRenderer with BSStandardF7FieldRendererImpl {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel

    override def renderOption(selected: Boolean, value: String, label: NodeSeq)(implicit hints: Seq[RenderHint]): Elem =
      <option selected={if (selected) "true" else null} value={value}>{label}</option>
  }

  implicit def multiSelectFieldRenderer: MultiSelectF7FieldRenderer = new MultiSelectF7FieldRenderer with BSStandardF7FieldRendererImpl {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel

    override def renderOption(selected: Boolean, value: String, label: NodeSeq)(implicit hints: Seq[RenderHint]): Elem =
      <option selected={if (selected) "true" else null} value={value}>{label}</option>
  }

  implicit def checkboxFieldRenderer: CheckboxF7FieldRenderer = new BSCheckboxF7FieldRendererImpl {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel
  }

  implicit def radioFieldRenderer: RadioF7FieldRenderer = new BSRadioF7FieldRendererImpl {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel
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
