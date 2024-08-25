package com.fastscala.templates.form6.fields

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
