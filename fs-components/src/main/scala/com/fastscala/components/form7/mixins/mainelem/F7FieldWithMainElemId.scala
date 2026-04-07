package com.fastscala.components.form7.mixins.mainelem

import com.fastscala.components.utils.{ElemWithId, ElemWithRandomId, Mutable}

trait F7FieldWithMainElemId extends F7FieldWithMainElem {
  var _id: () => Option[String] = () => None

  def id: Option[String] = _id()

  def id(v: Option[String]): this.type = mutate {
    _id = () => v
  }

  def id(v: String): this.type = mutate {
    _id = () => Some(v)
  }

  override def mainElemId: String = _id().getOrElse(super.mainElemId)
}
