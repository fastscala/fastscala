package com.fastscala.templates.bootstrap5.form7

import com.fastscala.templates.bootstrap5.form7.renderermodifiers.{CheckboxAlignment, CheckboxSide, CheckboxStyle}
import com.fastscala.templates.form7.*
import com.fastscala.templates.form7.fields.*
import com.fastscala.templates.form7.renderers.*
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.{Elem, NodeSeq}

abstract class BSForm7Renderers()(
  implicit
  checkboxAlignment: CheckboxAlignment.Value,
  checkboxStyle: CheckboxStyle.Value,
  checkboxSide: CheckboxSide.Value,
) {

  import com.fastscala.templates.bootstrap5.helpers.BSHelpers.*

  def defaultRequiredFieldLabel: String

  implicit val bsFormRenderer: BSForm7Renderers = this

  implicit val textFieldRenderer: TextF7FieldRenderer = new TextF7FieldRenderer with BSStandardF7FieldRendererImpl {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel
  }

  implicit val textareaFieldRenderer: TextareaF7FieldRenderer = new TextareaF7FieldRenderer with BSStandardF7FieldRendererImpl {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel
  }

  implicit val selectFieldRenderer: SelectF7FieldRenderer = new SelectF7FieldRenderer with BSStandardF7FieldRendererImpl {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel

    override def renderOption(selected: Boolean, value: String, label: NodeSeq)(implicit hints: Seq[RenderHint]): Elem =
      <option selected={if (selected) "true" else null} value={value}>{label}</option>
  }

  implicit val multiSelectFieldRenderer: MultiSelectF7FieldRenderer = new MultiSelectF7FieldRenderer with BSStandardF7FieldRendererImpl {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel

    override def renderOption(selected: Boolean, value: String, label: NodeSeq)(implicit hints: Seq[RenderHint]): Elem =
      <option selected={if (selected) "true" else null} value={value}>{label}</option>
  }

  implicit val checkboxFieldRenderer: CheckboxF7FieldRenderer = new BSCheckboxF7FieldRendererImpl()(checkboxAlignment, checkboxStyle, checkboxSide) {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel
  }

  implicit val radioFieldRenderer: RadioF7FieldRenderer = new BSRadioF7FieldRendererImpl()(checkboxAlignment, checkboxStyle, checkboxSide) {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel
  }

  //  implicit val fileUploadFieldRenderer = new FileUploadFieldRenderer {
  //    override def transformFormElem(field: F5FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = super.transformFormElem(field)(elem).mb_3
  //  }

  implicit val buttonFieldRenderer: ButtonF7FieldRenderer = new ButtonF7FieldRenderer {
    override def render(field: F7SubmitButtonField[_])(btn: Elem)(implicit hints: Seq[RenderHint]): Elem = {
      if (!field.enabled) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.addClass("d-grid gap-2 d-md-flex justify-content-md-end").withId(field.aroundId)(
        btn
          .withAttrIf(hints.contains(DisableFieldsHint), "disabled" -> "true")
      )
    }
  }

  implicit val formRenderer: F7FormRenderer = new F7FormRenderer {
    override def render(form: Elem): Elem = form.mb_5.w_100.addClass("form")
  }
}
