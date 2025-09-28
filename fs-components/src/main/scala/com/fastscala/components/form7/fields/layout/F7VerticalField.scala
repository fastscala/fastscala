package com.fastscala.components.form7.fields.layout

import com.fastscala.components.form7.*

class F7VerticalField()(childrenFields: F7Field*) extends F7ContainerFieldBase {
  override def aroundClass: String = ""

  override def children: Seq[(String, F7Field)] = childrenFields.map("" -> _)
  
  def withAppendedField(field: F7Field): F7VerticalField = new F7VerticalField()((childrenFields :+ field) *)
  
  def withPrependedField(field: F7Field): F7VerticalField = new F7VerticalField()((field +: childrenFields) *)
}

object F7VerticalField {
  def apply()(children: F7Field*) = new F7VerticalField()(children*)
}

