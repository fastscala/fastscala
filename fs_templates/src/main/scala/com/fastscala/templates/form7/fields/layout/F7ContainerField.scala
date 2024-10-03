package com.fastscala.templates.form7.fields.layout

import com.fastscala.templates.form7._

class F7ContainerField(val aroundClass: String)(val children: (String, F7Field)*) extends F7ContainerFieldBase

object F7ContainerField {
  def apply(aroundClass: String)(children: (String, F7Field)*) = new F7ContainerField(aroundClass)(children: _*)
}
