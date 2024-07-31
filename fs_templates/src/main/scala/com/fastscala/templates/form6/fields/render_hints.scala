package com.fastscala.templates.form6.fields

import com.fastscala.templates.form6.Form6

trait RenderHint

object FailedSaveStateHint extends RenderHint

object ShowValidationsHint extends RenderHint

object DisableFieldsHint extends RenderHint

object ReadOnlyFieldsHint extends RenderHint

object OnSaveRerender extends RenderHint

object SaveOnEnterHint extends RenderHint

trait Form6WithSaveOnEnterHint extends Form6 {
  override def formRenderHits(): Seq[RenderHint] = super.formRenderHits() :+ SaveOnEnterHint
}

