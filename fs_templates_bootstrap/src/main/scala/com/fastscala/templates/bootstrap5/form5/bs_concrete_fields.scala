package com.fastscala.templates.bootstrap5.form5

import com.fastscala.templates.bootstrap5.helpers.BSHelpers
import com.fastscala.templates.form5.fields.*


object BSHorizontalField {

  import BSHelpers.*

  def apply(
             enabled: () => Boolean = () => true
             , deps: Set[FormField] = Set()
           )(children: FormField*) =
    new F5HorizontalField(enabled, deps)(children.map[(String, FormField)](f => col.getClassAttr -> f): _*)
}
