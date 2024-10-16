package com.fastscala.templates.form7.fields

import com.fastscala.templates.form7.F7Field
import com.fastscala.templates.form7.mixins._
import com.fastscala.templates.utils.Mutable

trait F7SimpleField extends F7Field
  with Mutable
  with F7FieldWithOnChangedField {

  override def disabled(): Boolean = false

  override def readOnly(): Boolean = false

  override def fieldAndChildreenMatchingPredicate(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] = if (predicate.applyOrElse[F7Field, Boolean](this, _ => false)) List(this) else Nil

  override def deps: Set[F7Field] = Set()

  override def enabled(): Boolean = true
}
