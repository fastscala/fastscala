package com.fastscala.components.jstree.config

import com.fastscala.js.Js
import upickle.default.{macroW, Writer}

case class Data(
                 url: Option[String] = None,
                 data: Option[Js] = None
               )

object Data {

  import com.fastscala.components.utils.upickle.JsWriter

  implicit val writer: Writer[Data] = macroW
}

case class Core(
                 check_callback: Option[Boolean] = None,
                 data: Option[Data] = None,
               )

object Core {
  implicit val writer: Writer[Core] = macroW
}

case class JSTreeConfig(
                         core: Option[Core] = None,
                         plugins: Option[Seq[String]] = None
                       )

object JSTreeConfig {
  implicit val writer: Writer[JSTreeConfig] = macroW
}