package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.FSContext
import com.fastscala.xml.scala_xml.ScalaXmlElemUtils.RichElem

trait Table5Responsive extends Table5Base {

  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

  override def aroundClasses()(implicit fsc: FSContext): String = super.aroundClasses() + " " + table_responsive.getClassAttr
}
