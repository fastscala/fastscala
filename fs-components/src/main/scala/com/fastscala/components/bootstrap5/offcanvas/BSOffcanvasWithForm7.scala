package com.fastscala.components.bootstrap5.offcanvas

import com.fastscala.components.form7.{DefaultForm7, F7FormRenderer}
import com.fastscala.core.FSContext

import scala.xml.NodeSeq

abstract class BSOffcanvasWithForm7(
                                     val offcanvasHeaderTitle: String,
                                     val position: OffcanvasPosition,
                                   )(implicit formRenderer: F7FormRenderer) extends DefaultForm7()(formRenderer) with BSOffcanvasBase {

  override def offcanvasBodyContents()(implicit fsc: FSContext): NodeSeq = render()
}
