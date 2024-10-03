package com.fastscala.templates.form7

trait RenderHint

object FailedSaveStateHint extends RenderHint

object ShowValidationsHint extends RenderHint

object DisableFieldsHint extends RenderHint

object ReadOnlyFieldsHint extends RenderHint

object OnSaveRerender extends RenderHint

object SaveOnEnterHint extends RenderHint

trait Form7WithSaveOnEnterHint extends Form7 {
  override def formRenderHits(): Seq[RenderHint] = super.formRenderHits() :+ SaveOnEnterHint
}

