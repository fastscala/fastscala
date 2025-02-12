package com.fastscala.components.jstree.config

import com.fastscala.js.Js
import upickle.default.{macroW, Writer}
import com.fastscala.components.utils.upickle.JsWriter

case class ContextMenu(
                        select_node: Option[Boolean] = None,
                        show_at_node: Option[Boolean] = None,
                        items: Option[Js] = None
                      )

object ContextMenu {

  implicit val writer: Writer[ContextMenu] = macroW
}

case class Data(
                 url: Option[String] = None,
                 data: Option[Js] = None
               )

object Data {

  implicit val writer: Writer[Data] = macroW
}

case class Themes(
                  dots: Option[Boolean] = None,
                  icons: Option[Boolean] = Some(true),
                  ellipsis: Option[Boolean] = Some(true),
                  stripes: Option[Boolean] = Some(true),
                 )

object Themes {

  implicit val writer: Writer[Themes] = macroW
}

case class Core(
                 check_callback: Option[Boolean] = None,
                 data: Option[Data] = None,
                 themes: Option[Themes] = None,
               )

object Core {
  implicit val writer: Writer[Core] = macroW
}

case class JSTreeConfig(
                         core: Option[Core] = None,
                         contextmenu: Option[ContextMenu] = None,
                         plugins: Option[Seq[String]] = None
                       )

object JSTreeConfig {
  implicit val writer: Writer[JSTreeConfig] = macroW
}
