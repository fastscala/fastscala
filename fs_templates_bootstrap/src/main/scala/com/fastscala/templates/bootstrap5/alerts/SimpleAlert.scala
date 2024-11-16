package com.fastscala.templates.bootstrap5.alerts

import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.rerenderers.Rerenderer
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.{Elem, NodeSeq}

object SimpleAlert {

  import com.fastscala.templates.bootstrap5.helpers.BSHelpers.*

  def apply(
             contents: NodeSeq,
             closeBtn: Elem
           ): Rerenderer = JS.rerenderable(rerenderer => implicit fsc => {
    alert.alert_dismissible.fade.show.withRole("alert").apply {
      contents ++ closeBtn.addClass("btn-close").withType("button").withAttr("data-bs-dismiss" -> "alert")
    }
  })
}
