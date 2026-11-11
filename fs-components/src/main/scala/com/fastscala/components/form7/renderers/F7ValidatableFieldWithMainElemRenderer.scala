package com.fastscala.components.form7.renderers

import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.form7.mixins.F7FieldWithValidation
import com.fastscala.components.form7.mixins.mainelem.F7FieldWithMainElem
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.{Elem, NodeSeq}

trait F7ValidatableFieldWithMainElemRenderer {

  def defaultRequiredFieldLabel: String

  def render(
              field: F7FieldWithMainElem & F7FieldWithValidation,
            )(
              mainElem: Elem,
              label: Option[Elem],
              invalidFeedback: Option[Elem],
              validFeedback: Option[Elem],
              help: Option[Elem],
            ): Elem
}


