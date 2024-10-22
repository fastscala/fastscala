package com.fastscala.templates.form7.mixins

import com.fastscala.templates.form7.fields.text.F7FieldInputFieldMixin

import scala.xml.Elem


trait F7FieldWithValidFeedback extends F7FieldInputFieldMixin {
  var _validFeedback: () => Option[Elem] = () => None

  def validFeedback: Option[Elem] = _validFeedback()

  def validFeedback(v: Option[Elem]): this.type = mutate {
    _validFeedback = () => v
  }

  def validFeedback(v: Elem): this.type = mutate {
    _validFeedback = () => Some(v)
  }

  def validFeedback(v: String): this.type = mutate {
    _validFeedback = () => Some(<div>{v}</div>)
  }

  def validFeedbackStrF(f: () => String): this.type = mutate {
    _validFeedback = () => Some(<div>{f()}</div>)
  }
}