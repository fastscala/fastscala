package com.fastscala.components.form7

import scala.xml.NodeSeq

trait F7Event

case class ChangedFormState(from: Form7State.Value, to: Form7State.Value) extends F7Event

case class ChangedField(field: F7Field) extends F7Event

/**
 * Requested submit by, for example, pressing return.
 */
case class SuggestSubmit(byField: F7Field) extends F7Event

case class PostValidation(errors: Seq[(F7Field, NodeSeq)]) extends F7Event

object PreValidate extends F7Event

object PostValidate extends F7Event

object PreSubmit extends F7Event

object Submit extends F7Event

object PostSubmit extends F7Event
