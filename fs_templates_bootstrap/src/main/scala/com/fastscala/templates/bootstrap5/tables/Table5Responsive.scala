package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.{FSContext, FSXmlEnv}

trait Table5Responsive[E <: FSXmlEnv] extends Table5Base[E] {

  import com.fastscala.core.FSXmlUtils._
  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

  override def aroundClasses()(implicit fsc: FSContext): String = super.aroundClasses() + " " + table_responsive.getClassAttr
}
