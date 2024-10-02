package com.fastscala.templates.form7.fields

trait F7SimpleField extends StandardF7Field
  with F7FieldMixin
  with F7FieldWithValidations
  with F7FieldWithOnChangedField {

  override def disabled(): Boolean = false

  override def readOnly(): Boolean = false

  override def fieldsMatching(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] = if (predicate.applyOrElse[F7Field, Boolean](this, _ => false)) List(this) else Nil

  override def deps: Set[F7Field] = Set()

  override def enabled(): Boolean = true
}
