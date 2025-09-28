package com.fastscala.components.form7.mixins

import com.fastscala.components.utils.{ElemWithId, ElemWithRandomId, Mutable}

trait F7FieldWithId extends Mutable with ElemWithRandomId {
  var _id: () => Option[String] = () => None

  def id: Option[String] = _id()

  def id(v: Option[String]): this.type = mutate {
    _id = () => v
  }

  def id(v: String): this.type = mutate {
    _id = () => Some(v)
  }

  override def elemId: String = _id().getOrElse(super.elemId)
}
