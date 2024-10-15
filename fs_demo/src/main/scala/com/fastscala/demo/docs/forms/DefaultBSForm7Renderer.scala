package com.fastscala.demo.docs.forms

import com.fastscala.templates.bootstrap5.form7.BSForm7Renderers
import com.fastscala.templates.bootstrap5.form7.renderermodifiers.{CheckboxAlignment, CheckboxSide, CheckboxStyle}

object DefaultFSDemoBSForm7Renderers extends FSDemoBSForm7Renderers

class FSDemoBSForm7Renderers()(
  implicit
  checkboxAlignment: CheckboxAlignment.Value = CheckboxAlignment.default,
  checkboxStyle: CheckboxStyle.Value = CheckboxStyle.default,
  checkboxSide: CheckboxSide.Value = CheckboxSide.default,
) extends BSForm7Renderers() {
  override def defaultRequiredFieldLabel: String = "Required field"
}

