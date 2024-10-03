package com.fastscala.templates.form7.mixins

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7.{ChangedField, F7Event, Form7, RenderHint}
import com.fastscala.templates.utils.Mutable


trait F7FieldWithOnChangedField extends StandardF7Field with Mutable {

  var _onChangedField = collection.mutable.ListBuffer[F7OnChangedFieldHandler]()

  def onChangedField: Seq[F7OnChangedFieldHandler] = _onChangedField.toSeq

  def addOnChangedField(onchange: F7OnChangedFieldHandler): this.type = mutate {
    _onChangedField += onchange
  }

  override def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case ChangedField(field) => _onChangedField.map(_.onChanged(field)).reduceOption(_ & _).getOrElse(Js.void)
    case _ => Js.void
  })
}
