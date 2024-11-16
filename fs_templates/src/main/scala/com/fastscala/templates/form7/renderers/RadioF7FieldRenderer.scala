package com.fastscala.templates.form7.renderers

import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.templates.form7.fields.radio.F7RadioFieldBase
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.{Elem, NodeSeq}

trait RadioF7FieldRenderer extends StandardF7FieldRenderer {

  def render(
              field: F7RadioFieldBase[_],
            )(
              inputElemsAndLabels: Seq[(Elem, Option[Elem])],
              label: Option[Elem],
              invalidFeedback: Option[Elem],
              validFeedback: Option[Elem],
              help: Option[Elem],
            ): Elem

  def showOrUpdateValidation(
                              field: F7RadioFieldBase[_]
                            )(ns: NodeSeq): Js

  def hideValidation(
                      field: F7RadioFieldBase[_]
                    )(): Js
}
