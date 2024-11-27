package com.fastscala.components.form5.fields

trait FormEvent

case class ChangedField(field: FormField)(implicit renderedWithHints: Seq[RenderHint]) extends FormEvent

object BeforeSave extends FormEvent

object PerformSave extends FormEvent

object ErrorsOnSave extends FormEvent

object AfterSave extends FormEvent