package com.fastscala.demo.docs.forms

import com.fastscala.templates.bootstrap5.form6.BSForm6Renderer

object DefaultBSForm6Renderer extends BSForm6Renderer {
  override def defaultRequiredFieldLabel: String = "Required field"
}

