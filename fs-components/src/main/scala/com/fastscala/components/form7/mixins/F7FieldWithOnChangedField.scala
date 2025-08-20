package com.fastscala.components.form7.mixins

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.{JS, printBeforeExec}
import com.fastscala.components.form7.*
import com.fastscala.components.utils.Mutable
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem


trait F7FieldWithOnChangedField extends F7Field with Mutable {
  self =>

  var _onChangedField = collection.mutable.ListBuffer[F7OnChangedFieldHandler]()

  def onChangedField: Seq[F7OnChangedFieldHandler] = _onChangedField.toSeq

  def addOnChangedField(onchange: F7OnChangedFieldHandler): this.type = mutate {
    _onChangedField += onchange
  }

  def addOnThisFieldChanged(onChange: this.type => Js): this.type = mutate {
    _onChangedField += new F7OnChangedFieldHandler {
      override def onChanged(field: F7Field)(implicit form: Form7, fsc: FSContext): Js = if (field == self) onChange(self) else JS.void
    }
  }

  override def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext): Js = super.onEvent(event) & (event match {
    case ChangedField(field) => _onChangedField.map(_.onChanged(field)).reduceOption(_ & _).getOrElse(JS.void)
    case _ => JS.void
  })
}
