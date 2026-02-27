package com.fastscala.components.form7.renderers

import com.fastscala.components.form7.mixins.ValidatableF7Field

import scala.xml.Elem


trait StandardF7FieldRenderer {

  def defaultRequiredFieldLabel: String

  implicit class RichField(field: ValidatableF7Field) {
    def labelId = field.elemId + "-label"

    def invalidFeedbackId = field.elemId + "-invalid-feedback"

    def validFeedbackId = field.elemId + "-valid-feedback"

    def helpId = field.elemId + "-help"
  }
}


