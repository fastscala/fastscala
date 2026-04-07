package com.fastscala.components.form7.renderers

import com.fastscala.components.form7.fields.file.{F7FileUploadField, F7UploadedFile}
import com.fastscala.components.form7.mixins.F7FieldWithValidation
import com.fastscala.core.{FSContext, FSUploadedFile}
import com.fastscala.js.Js
import com.fastscala.scala_xml.rerenderers.RerendererP

import scala.xml.Elem

trait F7FileUploadFieldRenderer {

  def defaultRequiredFieldLabel: String

  def renderProgressBar(): Elem

  def renderProgress(progressBar: Elem): Elem

  def render(
              field: F7FileUploadField,
            )(
              inputElem: Elem,
              previewRerenderer: RerendererP[FSContext => Elem],
              progressElem: Elem,
              label: Option[Elem],
              invalidFeedback: Option[Elem],
              validFeedback: Option[Elem],
              help: Option[Elem],
            )(implicit fsc: FSContext): Elem
}
