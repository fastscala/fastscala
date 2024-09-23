package com.fastscala.templates.form6.fields

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form6.Form6

import scala.xml.NodeSeq

trait F6FieldWithValidations extends ValidatableF6Field with F6FieldMixin {
  var _validations = collection.mutable.ListBuffer[() => Option[NodeSeq]]()

  def addValidation(validate: () => Option[NodeSeq]): this.type = mutate {
    _validations += validate
  }

  def addValidation(valid_? : () => Boolean, error: NodeSeq): this.type = mutate {
    _validations += (() => if (!valid_?()) Some(error) else None)
  }

  override def errors(): Seq[(ValidatableF6Field, NodeSeq)] = super.errors() ++
    _validations.flatMap({
      case validation => validation()
    }).map(ns => this -> ns)
}

trait F6FieldWithOnChangedField extends F6FieldMixin {
  var _onChangedField = collection.mutable.ListBuffer[PartialFunction[F6Field, Js]]()

  def onChangedField: Seq[PartialFunction[F6Field, Js]] = _onChangedField.toSeq

  def addOnChangedField(onchange: PartialFunction[F6Field, Js]): this.type = mutate {
    _onChangedField += onchange
  }

  override def onEvent(event: FormEvent)(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case ChangedField(field) => _onChangedField.map(func => if (func.isDefinedAt(field)) func(field) else Js.void).reduceOption(_ & _).getOrElse(Js.void)
    case _ => Js.void
  })
}
