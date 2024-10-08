package com.fastscala.templates.form7.renderers

import com.fastscala.js.Js
import com.fastscala.templates.form7.mixins.StandardF7Field
import com.fastscala.xml.scala_xml.JS

import scala.xml.{Elem, NodeSeq}


trait StandardF7FieldRenderer {

  def defaultRequiredFieldLabel: String

  implicit class RichField(field: StandardF7Field) {
    def invalidFeedbackId = field.elemId + "-invalid-feedback"

    def validFeedbackId = field.elemId + "-valid-feedback"

    def helpId = field.elemId + "-help"
  }

  def render(
              field: StandardF7Field,
            )(
              inputElem: Elem,
              label: Option[Elem],
              invalidFeedback: Option[Elem],
              validFeedback: Option[Elem],
              help: Option[Elem],
            ): Elem

  def renderDisabled(field: StandardF7Field): Elem = <div style="display:none;" id={field.aroundId}></div>

  def showOrUpdateValidation(
                              field: StandardF7Field
                            )(ns: NodeSeq): Js =
    JS.setContents(field.invalidFeedbackId, ns) &
      JS.removeClass(field.invalidFeedbackId, "visually-hidden") &
      JS.addClass(field.validFeedbackId, "visually-hidden") &
      JS.addClass(field.elemId, "is-invalid") &
      JS.removeClass(field.elemId, "is-valid") &
      JS.setAttr(field.elemId)("aria-describedby", field.invalidFeedbackId)

  def hideValidation(
                      field: StandardF7Field
                    )(): Js =
    JS.addClass(field.invalidFeedbackId, "visually-hidden") &
      JS.removeClass(field.elemId, "is-invalid") &
      JS.removeAttr(field.elemId, "aria-describedby")
}


