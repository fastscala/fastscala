package com.fastscala.demo.docs.forms

import com.fastscala.templates.bootstrap5.form7.BSForm7Renderers

object DefaultBSForm7Renderers extends BSForm7Renderers {
  override def defaultRequiredFieldLabel: String = "Required field"
}

