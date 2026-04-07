package com.fastscala.components.form7.renderers

import com.fastscala.components.form7.fields.radio.F7RadioFieldBase
import com.fastscala.components.form7.mixins.F7FieldWithValidation
import com.fastscala.components.form7.mixins.mainelem.F7FieldWithMainElem
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS

import scala.xml.NodeSeq

object ValidationUtils {

  object F7FieldWithMainElemWithValidation {
    def showOrUpdateValidation(
                                field: F7FieldWithMainElem & F7FieldWithValidation
                              )(ns: NodeSeq): Js =
      JS.setContents(field.invalidFeedbackId, ns) &
        JS.removeClass(field.invalidFeedbackId, "visually-hidden") &
        JS.addClass(field.validFeedbackId, "visually-hidden") &
        JS.addClass(field.mainElemId, "is-invalid") &
        JS.removeClass(field.mainElemId, "is-valid") &
        JS.setAttr(field.mainElemId)("aria-describedby", field.invalidFeedbackId)

    def hideValidation(
                        field: F7FieldWithMainElem & F7FieldWithValidation
                      )(): Js =
      JS.addClass(field.invalidFeedbackId, "visually-hidden") &
        JS.removeClass(field.mainElemId, "is-invalid") &
        JS.removeAttr(field.mainElemId, "aria-describedby")
  }

  object RadioField {

    def showOrUpdateValidation(
                                field: F7RadioFieldBase[?]
                              )(ns: NodeSeq): Js =
      JS.setContents(field.invalidFeedbackId, ns) &
        JS.removeClass(field.invalidFeedbackId, "visually-hidden") &
        JS.addClass(field.validFeedbackId, "visually-hidden") &
        JS.addClassToElemsMatchingSelector(s"#${field.aroundId} > .form-check", "is-invalid") &
        JS.addClassToElemsMatchingSelector(s"#${field.aroundId} > .form-check > input", "is-invalid") &
        JS.removeClassFromElemsMatchingSelector(s"#${field.aroundId} > .form-check", "is-valid") &
        JS.removeClassFromElemsMatchingSelector(s"#${field.aroundId} > .form-check > input", "is-valid")

    def hideValidation(
                        field: F7RadioFieldBase[?]
                      ): Js =
      JS.addClass(field.invalidFeedbackId, "visually-hidden") &
        JS.removeClassFromElemsMatchingSelector(s"#${field.aroundId} > .form-check", "is-invalid") &
        JS.removeClassFromElemsMatchingSelector(s"#${field.aroundId} > .form-check > input", "is-invalid")
  }
}
