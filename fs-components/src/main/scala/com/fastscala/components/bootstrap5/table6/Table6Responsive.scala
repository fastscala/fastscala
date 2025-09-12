package com.fastscala.components.bootstrap5.table6

import com.fastscala.core.FSContext
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

trait Table6Responsive extends Table6Base {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  override def tableWrapperClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = super.tableWrapperClasses() + " " + table_responsive.getClassAttr
}
