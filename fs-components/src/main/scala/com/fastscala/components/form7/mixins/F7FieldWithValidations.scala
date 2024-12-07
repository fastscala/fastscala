package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.F7Field

import scala.xml.NodeSeq


trait F7FieldWithValidations extends F7Field {
  var _validations = collection.mutable.ListBuffer[() => Option[NodeSeq]]()

  def addValidation(validate: () => Option[NodeSeq]): this.type = mutate {
    _validations += validate
  }

  def addValidation(valid_? : () => Boolean, error: () => NodeSeq): this.type = mutate {
    _validations += (() => if (!valid_?()) Some(error()) else None)
  }

  def addValidation(valid_? : this.type => Boolean, error: this.type => NodeSeq): this.type = mutate {
    _validations += (() => if (!valid_?(this)) Some(error(this)) else None)
  }

  override def validate(): Seq[(F7Field, NodeSeq)] = super.validate() ++
    _validations.flatMap({
      case validation => validation()
    }).map(ns => this -> ns)
}
