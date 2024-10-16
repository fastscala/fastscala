package com.fastscala.templates.form7.mixins

import com.fastscala.templates.form7.F7Field
import com.fastscala.templates.utils.Mutable

import scala.xml.NodeSeq


trait F7FieldWithValidations extends Mutable {
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
