package com.fastscala.demo.docs

import com.fastscala.core.{FSContext, FSSession}
import com.fastscala.demo.db.User
import com.fastscala.demo.docs.bootstrap.{BootstrapImagesPage, BootstrapTypographyPage}
import com.fastscala.utils.{IdGen, RenderableWithFSContext}
import com.fastscala.xml.scala_xml.ScalaXmlRenderableWithFSContext
import jakarta.servlet.http.HttpServletRequest

import scala.xml.NodeSeq

case class Menu(items: MenuItem*) {
  def render()(implicit fsc: FSContext): NodeSeq = {
    <div class="position-sticky p-3 sidebar-sticky">
      <ul class="list-unstyled ps-0">
        {items.map(_.render())}
      </ul>
    </div>
  }

  def serve()(implicit req: HttpServletRequest, session: FSSession): Option[ScalaXmlRenderableWithFSContext] =
    items.map(_.serve()).find(_.isDefined).flatten
}

trait MenuItem {
  def render()(implicit fsc: FSContext): NodeSeq

  def serve()(implicit req: HttpServletRequest, session: FSSession): Option[ScalaXmlRenderableWithFSContext]

  def matches(uri: String): Boolean
}

case class MenuSection(name: String)(items: MenuItem*) extends MenuItem {

  def matches(uri: String): Boolean = items.exists(_.matches(uri))

  override def render()(implicit fsc: FSContext): NodeSeq = {
    val isOpen = items.exists(_.matches(fsc.page.req.getRequestURI))
    val id = IdGen.id
    <li class="mb-1">
      <button class={"text-white btn bi btn-toggle d-inline-flex align-items-center rounded border-0" + (if (isOpen) "" else " collapsed")} data-bs-toggle="collapse" data-bs-target={s"#$id"} aria-expanded={isOpen.toString}>
        {name}
      </button>
      <div class={"collapse" + (if (isOpen) " show" else "")} id={id}>
        <ul class="btn-toggle-nav list-unstyled fw-normal pb-1 small">
          {items.map(_.render())}
        </ul>
      </div>
    </li>
  }

  override def serve()(implicit req: HttpServletRequest, session: FSSession): Option[ScalaXmlRenderableWithFSContext] =
    items.map(_.serve()).find(_.isDefined).flatten
}

case class SimpleMenuItem(name: String, href: String) extends MenuItem {

  def matches(uri: String): Boolean = href == uri

  def serve()(implicit req: HttpServletRequest, session: FSSession): Option[ScalaXmlRenderableWithFSContext] = None

  def render()(implicit fsc: FSContext): NodeSeq =
    <li><a href={href} class="text-white d-inline-flex text-decoration-none rounded">{name}</a></li>
}

class RoutingMenuItem(matching: String*)(val name: String, page: () => ScalaXmlRenderableWithFSContext) extends MenuItem {

  def matches(uri: String): Boolean = href == uri

  def href: String = matching.mkString("/", "/", "")

  def render()(implicit fsc: FSContext): NodeSeq =
    <li><a href={href} class="text-white d-inline-flex text-decoration-none rounded">{name}</a></li>

  import com.fastscala.server.RoutingHandlerHelper._

  def serve()(implicit req: HttpServletRequest, session: FSSession): Option[ScalaXmlRenderableWithFSContext] = Some(req).collect {
    case Get(path@_*) if path == matching => page()
  }
}

class HeaderMenuItem(val title: String) extends MenuItem {
  override def render()(implicit fsc: FSContext): NodeSeq = <li class="mt-3"><span class="menu-heading fw-bold text-uppercase fs-7 ">{title}</span></li>

  override def serve()(implicit req: HttpServletRequest, session: FSSession): Option[ScalaXmlRenderableWithFSContext] = None

  override def matches(uri: String): Boolean = false
}