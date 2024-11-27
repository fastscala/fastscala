package com.fastscala.components.form7.fields.layout

import com.fastscala.components.form7.*

class F7VerticalField()(childrenFields: F7Field*) extends F7ContainerFieldBase {
  override def aroundClass: String = ""

  override def children: Seq[(String, F7Field)] = childrenFields.map("" -> _)
}

object F7VerticalField {
  def apply()(children: F7Field*) = new F7VerticalField()(children: _*)
}

