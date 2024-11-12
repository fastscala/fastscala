package com.fastscala.templates.form6.fields

trait F6SimpleField extends StandardF6Field
  with F6FieldMixin
  with F6FieldWithValidations
  with F6FieldWithOnChangedField {

  override def disabled: Boolean = false

  override def readOnly: Boolean = false

  override def fieldsMatching(predicate: PartialFunction[F6Field, Boolean]): List[F6Field] = if (predicate.applyOrElse[F6Field, Boolean](this, _ => false)) List(this) else Nil

  override def deps: Set[F6Field] = Set()

  override def enabled(): Boolean = true
}
