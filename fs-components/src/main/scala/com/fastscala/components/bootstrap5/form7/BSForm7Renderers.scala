package com.fastscala.components.bootstrap5.form7

import com.fastscala.components.bootstrap5.form7.renderermodifiers.{CheckboxAlignment, CheckboxSide, CheckboxStyle}
import com.fastscala.components.bootstrap5.form7.renderers.{BSAceEditorF7FieldRenderer, BSCheckboxF7FieldRenderer, BSF7FormRenderer, BSFormInputGroupF7FieldRenderer, BSRadioF7FieldRenderer, BSStandardF7FieldRenderer}
import com.fastscala.components.form7.*
import com.fastscala.components.form7.fields.*
import com.fastscala.components.form7.fields.submit.F7SubmitButtonField
import com.fastscala.components.form7.mixins.F7FieldWithValidation
import com.fastscala.components.form7.renderers.*
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.{Elem, NodeSeq}

abstract class BSForm7Renderers()(implicit checkboxAlignment: CheckboxAlignment.Value, checkboxStyle: CheckboxStyle.Value, checkboxSide: CheckboxSide.Value) {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  def defaultRequiredFieldLabel: String

  implicit val bsFormRenderer: BSForm7Renderers = this

  implicit val textFieldRenderer: TextF7FieldRenderer & BSStandardF7FieldRenderer = new TextF7FieldRenderer with BSStandardF7FieldRenderer {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel
  }

  implicit val textareaFieldRenderer: F7TextareaFieldRenderer & BSStandardF7FieldRenderer = new F7TextareaFieldRenderer with BSStandardF7FieldRenderer {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel
  }

  implicit val selectFieldRenderer: F7SelectFieldRenderer & BSStandardF7FieldRenderer = new F7SelectFieldRenderer with BSStandardF7FieldRenderer {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel

    override def renderOption(selected: Boolean, value: String, label: NodeSeq): Elem =
      <option selected={if (selected) "true" else null} value={value}>{label}</option>
  }

  implicit val multiSelectFieldRenderer: F7MultiSelectFieldRenderer & BSStandardF7FieldRenderer = new F7MultiSelectFieldRenderer with BSStandardF7FieldRenderer {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel

    override def renderOption(selected: Boolean, value: String, label: NodeSeq): Elem =
      <option selected={if (selected) "true" else null} value={value}>{label}</option>
  }

  implicit val checkboxFieldRenderer: BSCheckboxF7FieldRenderer = new BSCheckboxF7FieldRenderer()(using checkboxAlignment, checkboxStyle, checkboxSide) {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel
  }

  implicit val radioFieldRenderer: BSRadioF7FieldRenderer = new BSRadioF7FieldRenderer()(using checkboxAlignment, checkboxStyle, checkboxSide) {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel
  }

  implicit val inputGroupFieldRenderer: BSFormInputGroupF7FieldRenderer = new BSFormInputGroupF7FieldRenderer {}

  //  implicit val fileUploadFieldRenderer = new FileUploadFieldRenderer {
  //    override def transformFormElem(field: F5FileUploadField)(elem: Elem): Elem = super.transformFormElem(field)(elem).mb_3
  //  }

  implicit val buttonFieldRenderer: F7SubmitButtonFieldRenderer = new F7SubmitButtonFieldRenderer {
    override def render(field: F7SubmitButtonField[?])(btn: Elem): Elem = {
      if (!field.enabled) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.addClass("d-grid gap-2 d-md-flex justify-content-md-end").withId(field.aroundId)(btn)
    }
  }

  implicit val aceEditorFieldRenderer: BSAceEditorF7FieldRenderer = new BSAceEditorF7FieldRenderer {
    override def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel
  }

  implicit val formRenderer: BSF7FormRenderer = new BSF7FormRenderer()
}
