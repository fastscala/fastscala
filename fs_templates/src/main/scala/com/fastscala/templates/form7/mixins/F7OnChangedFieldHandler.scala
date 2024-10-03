package com.fastscala.templates.form7.mixins

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7.{F7Field, Form7, RenderHint}


trait F7OnChangedFieldHandler {
  def onChanged(field: F7Field)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js
}
