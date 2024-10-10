package com.fastscala.templates.form7.renderers

import com.fastscala.js.Js
import com.fastscala.templates.form7.mixins.StandardF7Field
import com.fastscala.xml.scala_xml.JS

import scala.xml.{Elem, NodeSeq}


trait StandardF7FieldRenderer {

  def defaultRequiredFieldLabel: String

  implicit class RichField(field: StandardF7Field) {
    def labelId = field.elemId + "-label"

    def invalidFeedbackId = field.elemId + "-invalid-feedback"

    def validFeedbackId = field.elemId + "-valid-feedback"

    def helpId = field.elemId + "-help"
  }

  def renderDisabled(field: StandardF7Field): Elem = <div style="display:none;" id={field.aroundId}></div>
}


