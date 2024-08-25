package com.fastscala.demo.docs.navigation

import com.fastscala.core.FSContext
import com.fastscala.utils.IdGen

import scala.xml.NodeSeq

object DefaultBSMenuRenderer {
  implicit val bsMenuRenderer = new DefaultBSMenuRenderer {}
  implicit val menuSectionRenderer = new DefaultMenuSectionRenderer {}
  implicit val simpleMenuItemRenderer = new DefaultSimpleMenuItemRenderer {}
  implicit val routingMenuItemRenderer = new DefaultRoutingMenuItemRenderer {}
  implicit val headerMenuItemRenderer = new DefaultHeaderMenuItemRenderer {}
}

trait DefaultBSMenuRenderer extends BSMenuRenderer {
  def render(elem: BSMenu)(implicit fsc: FSContext): NodeSeq = {
    <div class="position-sticky p-3 sidebar-sticky">
      <ul class="list-unstyled ps-0">
        {elem.items.map(_.render())}
      </ul>
    </div>
  }
}

trait DefaultMenuSectionRenderer extends MenuSectionRenderer {
  def render(elem: MenuSection)(implicit fsc: FSContext): NodeSeq = {
    val isOpen = elem.items.exists(_.matches(fsc.page.req.getRequestURI))
    val id = IdGen.id
    <li class="mb-1">
      <button class={"text-white btn bi btn-toggle d-inline-flex align-items-center rounded border-0" + (if (isOpen) "" else " collapsed")} data-bs-toggle="collapse" data-bs-target={s"#$id"} aria-expanded={isOpen.toString}>
        {elem.name}
      </button>
      <div class={"collapse" + (if (isOpen) " show" else "")} id={id}>
        <ul class="btn-toggle-nav list-unstyled fw-normal pb-1 small">
          {elem.items.map(_.render())}
        </ul>
      </div>
    </li>
  }
}

trait DefaultSimpleMenuItemRenderer extends SimpleMenuItemRenderer {
  def render(elem: SimpleMenuItem)(implicit fsc: FSContext): NodeSeq = <li><a href={elem.href} class="text-white d-inline-flex text-decoration-none rounded">{elem.name}</a></li>
}

trait DefaultRoutingMenuItemRenderer extends RoutingMenuItemRenderer {
  def render(elem: RoutingMenuItem)(implicit fsc: FSContext): NodeSeq = <li><a href={elem.href} class="text-white d-inline-flex text-decoration-none rounded">{elem.name}</a></li>
}

trait DefaultHeaderMenuItemRenderer extends HeaderMenuItemRenderer {
  def render(elem: HeaderMenuItem)(implicit fsc: FSContext): NodeSeq = <li class="mt-3"><span class="menu-heading fw-bold text-uppercase fs-7 ">{elem.title}</span></li>
}
