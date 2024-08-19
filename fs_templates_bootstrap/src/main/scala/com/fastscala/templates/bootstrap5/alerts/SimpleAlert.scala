package com.fastscala.templates.bootstrap5.alerts

import com.fastscala.js.rerenderers.Rerenderer
import com.fastscala.xml.scala_xml.{FSScalaXmlEnv, JS}

import scala.xml.{Elem, NodeSeq}

object SimpleAlert {

  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

  def apply(
             contents: NodeSeq,
             closeBtn: Elem
           ): Rerenderer[FSScalaXmlEnv.type] = JS.rerenderable(rerenderer => implicit fsc => {
    alert.alert_dismissible.fade.show.withRole("alert").apply {
      contents ++ closeBtn.addClass("btn-close").withType("button").withAttr("data-bs-dismiss" -> "alert")
    }
  })
}
