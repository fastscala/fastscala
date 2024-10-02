package com.fastscala.templates.form7.fields

import scala.xml.NodeSeq

trait F7Event

case class ChangedField(field: F7Field)(implicit renderedWithHints: Seq[RenderHint]) extends F7Event

/**
 * Requested submit by, for example, pressing return.
 */
case class RequestedSubmit(byField: F7Field)(implicit renderedWithHints: Seq[RenderHint]) extends F7Event

object PreValidation extends F7Event

object Validation extends F7Event

case class PostValidation(errors: Seq[(F7Field, NodeSeq)]) extends F7Event

object PreSubmit extends F7Event

object Submit extends F7Event

object PostSubmit extends F7Event