package com.fastscala.scala_xml.utils

import com.fastscala.js.Js

import scala.xml.{Elem, Null, TopScope, Unparsed, UnprefixedAttribute}

/**
 * A `<script type="{scriptType}">` element wrapping `js` in a CDATA-commented block, which keeps a reference to the
 * original [[Js]]. `scriptType` is "text/javascript" (see [[JsXmlUtils.inScriptTag]]) or "module" (see
 * [[JsXmlUtils.inScriptTagModule]]).
 */
class ElemWithJsInScript(val js: Js, val scriptType: String = "text/javascript") extends Elem(
  null,
  "script",
  new UnprefixedAttribute("type", scriptType, Null),
  TopScope,
  false,
  Unparsed(
    """
// <![CDATA[
""" + js +
      """
// ]]>
"""
  )
)
