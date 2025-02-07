package com.fastscala.components.aceeditor.json

import com.fastscala.js.{Js, JsFunc1}


case class BindKey(win: String, mac: String)

object BindKey {

  import upickle.default._
  import com.fastscala.components.utils.upickle.JsWriter

  implicit lazy val writer: Writer[BindKey] = macroW
}

case class Command(
                    name: String,
                    bindKey: BindKey,
                    exec: Js,
                  )

object Command {

  import upickle.default._
  import com.fastscala.components.utils.upickle.JsWriter

  implicit lazy val writer: Writer[Command] = macroW
}
