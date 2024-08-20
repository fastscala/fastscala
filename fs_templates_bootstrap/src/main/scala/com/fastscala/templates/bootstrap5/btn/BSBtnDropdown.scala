package com.fastscala.templates.bootstrap5.components

import com.fastscala.core.FSContext
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.xml.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromElems

import scala.xml.Elem

object BSBtnDropdown {

  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

  def apply(btn: BSBtn, rightAlignedMenu: Boolean = false)(btns: BSBtn*)(implicit fsc: FSContext): Elem = {
    custom(btn, rightAlignedMenu)(btns.map(btn => btn.btnLink.withClass("dropdown-item")): _*)
  }

  def custom(btn: BSBtn, rightAlignedMenu: Boolean = false)(elems: Elem*)(implicit fsc: FSContext): Elem = {
    div.withClass("btn-group").apply {
      btn.btn.withType("button").withClass("dropdown-toggle")
        .withAttr("data-bs-toggle" -> "dropdown")
        .withAttr("aria-expanded" -> "false") ++
        ul.withClass("dropdown-menu").withClassIf(rightAlignedMenu, "dropdown-menu-end").apply {
          elems.map(elem => li.apply(elem.withClass("dropdown-item"))).mkNS
        }
    }
  }
}
