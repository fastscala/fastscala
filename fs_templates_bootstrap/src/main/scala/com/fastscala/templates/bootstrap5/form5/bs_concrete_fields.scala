package com.fastscala.templates.bootstrap5.form5

import com.fastscala.templates.bootstrap5.classes.BSHelpers
import com.fastscala.templates.form5.fields._
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem


object BSHorizontalField {

  import BSHelpers._

  def apply(
             enabled: () => Boolean = () => true
             , deps: Set[FormField] = Set()
           )(children: FormField*) =
    new F5HorizontalField(enabled, deps)(children.map[(String, FormField)](f => col.getClassAttr -> f): _*)
}
