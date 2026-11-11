package com.fastscala.components.form6.fields

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.form6.Form6
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.NodeSeq

trait F6FieldWithValidations extends ValidatableF6Field with F6FieldMixin {
  var _validations = collection.mutable.ListBuffer[() => Option[NodeSeq]]()

  def addValidation(validate: () => Option[NodeSeq]): this.type = mutate {
    _validations += validate
  }

  def addValidation(valid_? : () => Boolean, error: () => NodeSeq): this.type = mutate {
    _validations += (() => if (!valid_?()) Some(error()) else None)
  }

  override def errors(): Seq[(ValidatableF6Field, NodeSeq)] = super.errors() ++
    _validations.flatMap({
      case validation => validation()
    }).map(ns => this -> ns)
}

trait F6OnChangedFieldHandler {
  def onChanged(field: F6Field)(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js
}

trait F6FieldWithOnChangedField extends F6FieldMixin {

  var _onChangedField = collection.mutable.ListBuffer[F6OnChangedFieldHandler]()

  def onChangedField: Seq[F6OnChangedFieldHandler] = _onChangedField.toSeq

  def addOnChangedField(onchange: F6OnChangedFieldHandler): this.type = mutate {
    _onChangedField += onchange
  }

  override def onEvent(event: FormEvent)(implicit form: Form6, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & (event match {
    case ChangedField(field) => _onChangedField.map(_.onChanged(field)).reduceOption(_ & _).getOrElse(JS.void)
    case _ => JS.void
  })
}
