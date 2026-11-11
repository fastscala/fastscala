package com.fastscala.components.bootstrap5.table5

import com.fastscala.core.FSContext
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

trait Table5Responsive extends Table5Base {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  override def aroundClasses()(implicit fsc: FSContext): String = super.aroundClasses() + " " + table_responsive.getClassAttr
}
