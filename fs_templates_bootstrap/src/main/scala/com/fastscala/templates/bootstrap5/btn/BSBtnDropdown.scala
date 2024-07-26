package com.fastscala.templates.bootstrap5.components

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.templates.bootstrap5.utils.BSBtn

object BSBtnDropdown {

  import com.fastscala.core.FSXmlUtils._
  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

  def apply[E <: FSXmlEnv : FSXmlSupport](btn: BSBtn[E], rightAlignedMenu: Boolean = false)(btns: BSBtn[E]*)(implicit fsc: FSContext): E#Elem = {
    custom(btn, rightAlignedMenu)(btns.map(btn => btn.btnLink.withClass("dropdown-item")): _*)
  }

  def custom[E <: FSXmlEnv : FSXmlSupport](btn: BSBtn[E], rightAlignedMenu: Boolean = false)(elems: E#Elem*)(implicit fsc: FSContext): E#Elem = {
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
