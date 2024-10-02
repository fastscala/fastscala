package com.fastscala.demo.docs.forms

import com.fastscala.templates.bootstrap5.form7.BSForm7Renderer

object DefaultBSForm7Renderer extends BSForm7Renderer {
  override def defaultRequiredFieldLabel: String = "Required field"
}

