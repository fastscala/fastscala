package com.fastscala.components.form7.renderers

import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.form7.mixins.F7FieldWithValidation
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.{Elem, NodeSeq}

trait F7InputValidatableFieldRenderer extends F7ValidatableFieldRenderer {

  def render(
              field: F7FieldWithValidation,
            )(
              inputElem: Elem,
              label: Option[Elem],
              invalidFeedback: Option[Elem],
              validFeedback: Option[Elem],
              help: Option[Elem],
            ): Elem
}


