package com.fastscala.components.utils.upickle

import com.fastscala.js.Js
import upickle.core.Visitor
import upickle.default.*

implicit val JsWriter: Writer[Js] = new Writer[Js] {
  override def isJsonDictKey = false

  override def write0[V](out: Visitor[?, V], v: Js) = out.visitFloat64StringParts(v.cmd, -1, -1, -1)
}
