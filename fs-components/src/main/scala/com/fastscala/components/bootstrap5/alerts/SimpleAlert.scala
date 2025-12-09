package com.fastscala.components.bootstrap5.alerts

import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.rerenderers.Rerenderer

import scala.xml.{Elem, NodeSeq}

object SimpleAlert {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  def apply(
             contents: NodeSeq,
             closeBtn: Elem
           ): Elem = alert.alert_dismissible.fade.show.withRole("alert").apply {
    contents ++ closeBtn.addClass("btn-close").withType("button").withAttr("data-bs-dismiss" -> "alert")
  }
}
