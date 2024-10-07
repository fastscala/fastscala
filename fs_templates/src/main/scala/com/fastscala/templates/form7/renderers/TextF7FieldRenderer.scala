package com.fastscala.templates.form7.renderers

import com.fastscala.js.Js
import com.fastscala.templates.form7.fields.text.F7TextField

import scala.xml.{Elem, NodeSeq}


trait TextF7FieldRenderer {

  def defaultRequiredFieldLabel: String

  def render(
              field: F7TextField[_]
            )(
              inputElem: Elem,
              label: Option[Elem],
              invalidFeedback: Option[Elem],
              validFeedback: Option[Elem],
              help: Option[Elem],
            ): Elem

  def showValidation(
                      field: F7TextField[_]
                    )(ns: NodeSeq): Js

  def hideValidation(
                      field: F7TextField[_]
                    )(): Js
}


