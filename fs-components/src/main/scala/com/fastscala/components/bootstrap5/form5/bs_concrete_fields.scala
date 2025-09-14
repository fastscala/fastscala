package com.fastscala.components.bootstrap5.form5

import com.fastscala.components.bootstrap5.helpers.BSHelpers
import com.fastscala.components.form5.fields.*


object BSHorizontalField {

  import BSHelpers.*

  def apply(
             enabled: () => Boolean = () => true
             , deps: Set[FormField] = Set()
           )(children: FormField*) =
    new F5HorizontalField(enabled, deps)(children.map[(String, FormField)](f => col.getClassAttr -> f)*)
}
