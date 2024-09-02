package com.fastscala.demo.docs.navigation

import com.fastscala.core.{FSContext, FSSession}
import com.fastscala.utils.IdGen
import com.fastscala.xml.scala_xml.ScalaXmlRenderableWithFSContext

import org.eclipse.jetty.server.Request

import scala.xml.NodeSeq

case class BSMenu(items: MenuItem*)(implicit renderer: BSMenuRenderer) {
  def render()(implicit fsc: FSContext): NodeSeq = renderer.render(this)

  def serve()(implicit req: Request, session: FSSession): Option[ScalaXmlRenderableWithFSContext] =
    items.map(_.serve()).find(_.isDefined).flatten
}

case class BSNav(items: MenuItem*)(implicit renderer: BSNavBarRenderer) {

  val navBarId = IdGen.id("navBar")

  def render()(implicit fsc: FSContext): NodeSeq = renderer.render(this)

  def serve()(implicit req: Request, session: FSSession): Option[ScalaXmlRenderableWithFSContext] =
    items.map(_.serve()).find(_.isDefined).flatten
}

trait MenuItem {
  def render()(implicit fsc: FSContext): NodeSeq

  def serve()(implicit req: Request, session: FSSession): Option[ScalaXmlRenderableWithFSContext]

  def matches(uri: String): Boolean
}

case class MenuSection(name: String)(val items: MenuItem*)(implicit renderer: MenuSectionRenderer) extends MenuItem {

  def matches(uri: String): Boolean = items.exists(_.matches(uri))

  override def render()(implicit fsc: FSContext): NodeSeq = renderer.render(this)

  override def serve()(implicit req: Request, session: FSSession): Option[ScalaXmlRenderableWithFSContext] =
    items.map(_.serve()).find(_.isDefined).flatten
}

case class SimpleMenuItem(name: String, href: String)(implicit renderer: SimpleMenuItemRenderer) extends MenuItem {

  def matches(uri: String): Boolean = href == uri

  def serve()(implicit req: Request, session: FSSession): Option[ScalaXmlRenderableWithFSContext] = None

  def render()(implicit fsc: FSContext): NodeSeq = renderer.render(this)
}

class RoutingMenuItem(matching: String*)(val name: String, page: () => ScalaXmlRenderableWithFSContext)(implicit renderer: RoutingMenuItemRenderer) extends MenuItem {

  def matches(uri: String): Boolean = href == uri

  def href: String = matching.mkString("/", "/", "")

  def render()(implicit fsc: FSContext): NodeSeq = renderer.render(this)

  import com.fastscala.server.RoutingHandlerHelper._

  def serve()(implicit req: Request, session: FSSession): Option[ScalaXmlRenderableWithFSContext] = Some(req).collect {
    case Get(path@_*) if path == matching => page()
  }
}

class HeaderMenuItem(val title: String)(implicit renderer: HeaderMenuItemRenderer) extends MenuItem {
  override def render()(implicit fsc: FSContext): NodeSeq = renderer.render(this)

  override def serve()(implicit req: Request, session: FSSession): Option[ScalaXmlRenderableWithFSContext] = None

  override def matches(uri: String): Boolean = false
}

