package com.fastscala.demo.docs.bootstrap

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.SingleCodeExamplePage
import com.fastscala.templates.bootstrap5.modals.BSModal5Base
import com.fastscala.templates.bootstrap5.utils.BSBtn

import scala.xml.NodeSeq

class BootstrapModalPage extends SingleCodeExamplePage() {

  override def pageTitle: String = "Simple Modal Example"

  override def renderExampleContents()(implicit fsc: FSContext): NodeSeq = {
    // === code snippet ===
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._
    div.d_grid.mx_auto.col_8.my_5.apply {
      BSBtn().BtnPrimary.lbl("Open Modal").ajax(implicit fsc => new BSModal5Base {
        override def modalHeaderTitle: String = "Simple Modal"

        override def modalBodyContents()(implicit fsc: FSContext): NodeSeq =
          span.apply("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed posuere nec nisl non blandit. Ut vel libero dictum, feugiat risus quis, placerat lorem. Nam eleifend egestas pulvinar. Vestibulum non viverra sapien, at hendrerit ex. Vestibulum tempor eu risus ut vestibulum. Nullam semper vitae ex quis vestibulum. Fusce posuere, purus non consequat scelerisque, nulla risus bibendum diam, finibus mollis ipsum ligula eu enim.")

        override def modalFooterContents()(implicit fsc: FSContext): Option[NodeSeq] = {
          Some(BSBtn().BtnPrimary.lbl("Hide Modal").onclick(hide() & remove()).btn)
        }
      }.installAndShow()).btn
    }
    // === code snippet ===
  }
}
