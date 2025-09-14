package com.fastscala.components.form5.fields


import scala.xml.{Elem, NodeSeq}

trait TextFieldRenderer {

  def defaultRequiredFieldLabel: String

  def render[T](field: F5TextField[T])(label: Option[NodeSeq], inputElem: Elem, error: Option[NodeSeq])(implicit hints: Seq[RenderHint]): Elem
}

trait TextareaFieldRenderer {

  def defaultRequiredFieldLabel: String

  def render(field: F5TextAreaField)(label: Option[NodeSeq], inputElem: Elem, error: Option[NodeSeq])(implicit hints: Seq[RenderHint]): Elem
}

trait SelectFieldRenderer {

  def defaultRequiredFieldLabel: String

  def render[T](field: F5SelectField[T])(label: Option[Elem], elem: Elem, error: Option[NodeSeq])(implicit hints: Seq[RenderHint]): Elem
}

trait MultiSelectFieldRenderer {

  def defaultRequiredFieldLabel: String

  def render[T](field: F5MultiSelectField[T])(label: Option[Elem], elem: Elem, error: Option[NodeSeq])(implicit hints: Seq[RenderHint]): Elem
}

trait CheckboxFieldRenderer {

  def render(field: F5CheckboxField)(label: Option[Elem], elem: Elem, error: Option[NodeSeq])(implicit hints: Seq[RenderHint]): Elem
}

trait ButtonFieldRenderer {
  def render(field: F5SaveButtonField[?])(btn: Elem)(implicit hints: Seq[RenderHint]): Elem
}

trait FileUploadFieldRenderer {

  def transformFormElem(field: F5FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem

  def transforLabelElem(field: F5FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem

  def transforSubmitButtonElem(field: F5FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem

  def transforResetButtonElem(field: F5FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem

  def transforFileInputElem(field: F5FileUploadField)(elem: Elem)(implicit hints: Seq[RenderHint]): Elem = elem
}
