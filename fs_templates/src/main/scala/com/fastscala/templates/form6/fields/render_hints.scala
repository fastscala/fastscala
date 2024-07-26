package com.fastscala.templates.form6.fields

import com.fastscala.core.FSXmlEnv
import com.fastscala.templates.form6.Form6

trait RenderHint

object FailedSaveStateHint extends RenderHint

object ShowValidationsHint extends RenderHint

object DisableFieldsHint extends RenderHint

object ReadOnlyFieldsHint extends RenderHint

object OnSaveRerender extends RenderHint

object SaveOnEnterHint extends RenderHint

trait Form6WithSaveOnEnterHint[E <: FSXmlEnv] extends Form6[E] {
  override def formRenderHits(): Seq[RenderHint] = super.formRenderHits() :+ SaveOnEnterHint
}

