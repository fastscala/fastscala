package com.fastscala.components.form7.renderers

import com.fastscala.components.form7.mixins.F7FieldWithValidation

import scala.xml.Elem


trait F7StandardFieldRenderer {

  def defaultRequiredFieldLabel: String

  implicit class RichField(field: F7FieldWithValidation) {
    def labelId = field.elemId + "-label"

    def invalidFeedbackId = field.elemId + "-invalid-feedback"

    def validFeedbackId = field.elemId + "-valid-feedback"

    def helpId = field.elemId + "-help"
  }
}


