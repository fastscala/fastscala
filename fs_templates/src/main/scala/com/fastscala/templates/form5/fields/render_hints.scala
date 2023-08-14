package com.fastscala.templates.form5.fields

import com.fastscala.templates.form5.Form5

trait RenderHint

object ShowValidationsHint extends RenderHint

object DisableFieldsHint extends RenderHint

object ReadOnlyFieldsHint extends RenderHint

object OnSaveRerender extends RenderHint

object SaveOnEnterHint extends RenderHint

trait Form5WithSaveOnEnterHint extends Form5 {
  override def formRenderHits(): Seq[RenderHint] = super.formRenderHits() :+ SaveOnEnterHint
}

