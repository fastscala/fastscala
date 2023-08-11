package com.fastscala.templates.bootstrap5.alerts

import com.fastscala.js.{Js, Rerenderer}

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

object SimpleAlert {

  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

  def apply(
             contents: NodeSeq,
             closeBtn: Elem
           ): Rerenderer = Js.rerenderable(rerenderer => implicit fsc => {
    alert.alert_dismissible.fade.show.withRole("alert") {
      contents ++ closeBtn.addClass("btn-close").withType("button").withAttr("data-bs-dismiss" -> "alert")
    }
  })
}
