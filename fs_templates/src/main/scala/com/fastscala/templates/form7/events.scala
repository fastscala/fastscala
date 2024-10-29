package com.fastscala.templates.form7

import scala.xml.NodeSeq

trait F7Event

case class ChangedField(field: F7Field) extends F7Event

/**
 * Requested submit by, for example, pressing return.
 */
case class RequestedSubmit(byField: F7Field) extends F7Event

case class PostValidation(errors: Seq[(F7Field, NodeSeq)]) extends F7Event

object Submit extends F7Event
