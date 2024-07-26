package com.fastscala.templates.form5.fields


import com.fastscala.core.{FSXmlEnv, FSXmlSupport}

trait TextFieldRenderer {

  def defaultRequiredFieldLabel: String

  def render[E <: FSXmlEnv : FSXmlSupport, T](field: F5TextField[E, T])(label: Option[E#NodeSeq], inputElem: E#Elem, error: Option[E#NodeSeq])(implicit hints: Seq[RenderHint]): E#Elem
}

trait TextareaFieldRenderer {

  def defaultRequiredFieldLabel: String

  def render[E <: FSXmlEnv : FSXmlSupport](field: F5TextAreaField[E])(label: Option[E#NodeSeq], inputElem: E#Elem, error: Option[E#NodeSeq])(implicit hints: Seq[RenderHint]): E#Elem
}

trait SelectFieldRenderer {

  def defaultRequiredFieldLabel: String

  def render[E <: FSXmlEnv : FSXmlSupport, T](field: F5SelectField[E, T])(label: Option[E#Elem], elem: E#Elem, error: Option[E#NodeSeq])(implicit hints: Seq[RenderHint]): E#Elem
}

trait MultiSelectFieldRenderer {

  def defaultRequiredFieldLabel: String

  def render[E <: FSXmlEnv : FSXmlSupport, T](field: F5MultiSelectField[E, T])(label: Option[E#Elem], elem: E#Elem, error: Option[E#NodeSeq])(implicit hints: Seq[RenderHint]): E#Elem
}

trait CheckboxFieldRenderer {

  def render[E <: FSXmlEnv : FSXmlSupport](field: F5CheckboxField[E])(label: Option[E#Elem], elem: E#Elem, error: Option[E#NodeSeq])(implicit hints: Seq[RenderHint]): E#Elem
}

trait ButtonFieldRenderer {
  def render[E <: FSXmlEnv : FSXmlSupport](field: F5SaveButtonField[E, _])(btn: E#Elem)(implicit hints: Seq[RenderHint]): E#Elem
}

trait FileUploadFieldRenderer {

  def transformFormElem[E <: FSXmlEnv : FSXmlSupport](field: F5FileUploadField[E])(elem: E#Elem)(implicit hints: Seq[RenderHint]): E#Elem = elem

  def transforLabelElem[E <: FSXmlEnv : FSXmlSupport](field: F5FileUploadField[E])(elem: E#Elem)(implicit hints: Seq[RenderHint]): E#Elem = elem

  def transforSubmitButtonElem[E <: FSXmlEnv : FSXmlSupport](field: F5FileUploadField[E])(elem: E#Elem)(implicit hints: Seq[RenderHint]): E#Elem = elem

  def transforResetButtonElem[E <: FSXmlEnv : FSXmlSupport](field: F5FileUploadField[E])(elem: E#Elem)(implicit hints: Seq[RenderHint]): E#Elem = elem

  def transforFileInputElem[E <: FSXmlEnv : FSXmlSupport](field: F5FileUploadField[E])(elem: E#Elem)(implicit hints: Seq[RenderHint]): E#Elem = elem
}
