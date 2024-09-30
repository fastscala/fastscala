package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.FSContext

trait Table5Responsive extends Table5Base {

  import com.fastscala.templates.bootstrap5.helpers.BSHelpers._

  override def aroundClasses()(implicit fsc: FSContext): String = super.aroundClasses() + " " + table_responsive.getClassAttr
}
