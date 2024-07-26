package com.fastscala.templates.bootstrap5.form5

import com.fastscala.core.{FSXmlEnv, FSXmlSupport}
import com.fastscala.templates.bootstrap5.classes.BSHelpers
import com.fastscala.templates.form5.fields._


object BSHorizontalField {

  import BSHelpers._
  import com.fastscala.core.FSXmlUtils._

  def apply[E <: FSXmlEnv : FSXmlSupport](
                                           enabled: () => Boolean = () => true
                                           , deps: Set[FormField[_]] = Set()
                                         )(children: FormField[E]*) =
    new F5HorizontalField[E](enabled, deps)(children.map[(String, FormField[E])](f => col.getClassAttr -> f): _*)
}
