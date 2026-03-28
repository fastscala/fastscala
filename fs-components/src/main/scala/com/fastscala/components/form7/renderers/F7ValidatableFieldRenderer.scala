package com.fastscala.components.form7.renderers

import com.fastscala.components.form7.mixins.F7FieldWithValidation
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.xml.{Elem, NodeSeq}

trait F7ValidatableFieldRenderer extends F7StandardFieldRenderer {

  def showOrUpdateValidation(
                              field: F7FieldWithValidation
                            )(ns: NodeSeq): Js =
    JS.setContents(field.invalidFeedbackId, ns) &
      JS.removeClass(field.invalidFeedbackId, "visually-hidden") &
      JS.addClass(field.validFeedbackId, "visually-hidden") &
      JS.addClass(field.elemId, "is-invalid") &
      JS.removeClass(field.elemId, "is-valid") &
      JS.setAttr(field.elemId)("aria-describedby", field.invalidFeedbackId)

  def hideValidation(
                      field: F7FieldWithValidation
                    )(): Js =
    JS.addClass(field.invalidFeedbackId, "visually-hidden") &
      JS.removeClass(field.elemId, "is-invalid") &
      JS.removeAttr(field.elemId, "aria-describedby")
}


