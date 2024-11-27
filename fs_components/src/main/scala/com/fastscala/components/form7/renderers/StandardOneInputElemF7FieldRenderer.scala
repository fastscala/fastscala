package com.fastscala.components.form7.renderers

import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.form7.mixins.StandardF7Field
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.{Elem, NodeSeq}

trait StandardOneInputElemF7FieldRenderer extends StandardF7FieldRenderer {

  def render(
              field: StandardF7Field,
            )(
              inputElem: Elem,
              label: Option[Elem],
              invalidFeedback: Option[Elem],
              validFeedback: Option[Elem],
              help: Option[Elem],
            ): Elem

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


