package com.fastscala.components.bootstrap5.form7

import com.fastscala.components.bootstrap5.form7.renderermodifiers.{CheckboxAlignment, CheckboxSide, CheckboxStyle}
import com.fastscala.components.form7.*
import com.fastscala.components.form7.fields.*
import com.fastscala.components.form7.fields.submit.F7SubmitButtonField
import com.fastscala.components.form7.renderers.*
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.{Elem, NodeSeq}

abstract class BSForm7Renderers()(implicit checkboxAlignment: CheckboxAlignment.Value, checkboxStyle: CheckboxStyle.Value, checkboxSide: CheckboxSide.Value) {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  def defaultRequiredFieldLabel: String

  implicit val bsFormRenderer: BSForm7Renderers = this

  implicit val textFieldRenderer: TextF7FieldRenderer & BSStandardF7FieldRendererImpl = new TextF7FieldRenderer with BSStandardF7FieldRendererImpl {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel
  }

  implicit val textareaFieldRenderer: TextareaF7FieldRenderer & BSStandardF7FieldRendererImpl= new TextareaF7FieldRenderer with BSStandardF7FieldRendererImpl {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel
  }

  implicit val selectFieldRenderer: SelectF7FieldRenderer & BSStandardF7FieldRendererImpl= new SelectF7FieldRenderer with BSStandardF7FieldRendererImpl {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel

    override def renderOption(selected: Boolean, value: String, label: NodeSeq): Elem =
      <option selected={if (selected) "true" else null} value={value}>{label}</option>
  }

  implicit val multiSelectFieldRenderer: MultiSelectF7FieldRenderer & BSStandardF7FieldRendererImpl= new MultiSelectF7FieldRenderer with BSStandardF7FieldRendererImpl {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel

    override def renderOption(selected: Boolean, value: String, label: NodeSeq): Elem =
      <option selected={if (selected) "true" else null} value={value}>{label}</option>
  }

  implicit val checkboxFieldRenderer: BSCheckboxF7FieldRendererImpl = new BSCheckboxF7FieldRendererImpl()(using checkboxAlignment, checkboxStyle, checkboxSide) {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel
  }

  implicit val radioFieldRenderer: BSRadioF7FieldRendererImpl = new BSRadioF7FieldRendererImpl()(using checkboxAlignment, checkboxStyle, checkboxSide) {
    def defaultRequiredFieldLabel: String = BSForm7Renderers.this.defaultRequiredFieldLabel
  }

  //  implicit val fileUploadFieldRenderer = new FileUploadFieldRenderer {
  //    override def transformFormElem(field: F5FileUploadField)(elem: Elem): Elem = super.transformFormElem(field)(elem).mb_3
  //  }

  implicit val buttonFieldRenderer: ButtonF7FieldRenderer = new ButtonF7FieldRenderer {
    override def render(field: F7SubmitButtonField[?])(btn: Elem): Elem = {
      if (!field.enabled) div.withId(field.aroundId).withStyle(";display:none;")
      else div.mb_3.addClass("d-grid gap-2 d-md-flex justify-content-md-end").withId(field.aroundId)(btn)
    }
  }

  implicit val formRenderer: BSF7FormRendererImpl = new BSF7FormRendererImpl()
}
