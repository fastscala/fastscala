package com.fastscala.templates.form7.fields

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7.Form7

import scala.xml.NodeSeq

trait F7FieldWithValidations extends ValidatableF7Field with F7FieldMixin {
  var _validations = collection.mutable.ListBuffer[() => Option[NodeSeq]]()

  def addValidation(validate: () => Option[NodeSeq]): this.type = mutate {
    _validations += validate
  }

  def addValidation(valid_? : () => Boolean, error: () => NodeSeq): this.type = mutate {
    _validations += (() => if (!valid_?()) Some(error()) else None)
  }

  override def errors(): Seq[(ValidatableF7Field, NodeSeq)] = super.errors() ++
    _validations.flatMap({
      case validation => validation()
    }).map(ns => this -> ns)
}

trait F7OnChangedFieldHandler {
  def onChanged(field: F7Field)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js
}

trait F7FieldWithOnChangedField extends F7FieldMixin {

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
