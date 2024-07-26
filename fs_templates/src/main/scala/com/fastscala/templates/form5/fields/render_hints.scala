package com.fastscala.templates.form5.fields

import com.fastscala.core.FSXmlEnv
import com.fastscala.templates.form5.Form5

trait RenderHint

object FailedSaveStateHint extends RenderHint

object ShowValidationsHint extends RenderHint

object DisableFieldsHint extends RenderHint

object ReadOnlyFieldsHint extends RenderHint

object OnSaveRerender extends RenderHint

object SaveOnEnterHint extends RenderHint

trait Form5WithSaveOnEnterHint[E <: FSXmlEnv] extends Form5[E] {
  override def formRenderHits(): Seq[RenderHint] = super.formRenderHits() :+ SaveOnEnterHint
}

