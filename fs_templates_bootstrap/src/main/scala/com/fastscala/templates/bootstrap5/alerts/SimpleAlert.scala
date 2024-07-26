package com.fastscala.templates.bootstrap5.alerts

import com.fastscala.core.{FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.js.rerenderers.Rerenderer

object SimpleAlert {

  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

  def apply[E <: FSXmlEnv : FSXmlSupport](
                                           contents: E#NodeSeq,
                                           closeBtn: E#Elem
                                         ): Rerenderer[E] = Js.rerenderable(rerenderer => implicit fsc => {
    import com.fastscala.core.FSXmlUtils._
    alert.alert_dismissible.fade.show.withRole("alert").apply {
      contents ++ closeBtn.addClass("btn-close").withType("button").withAttr("data-bs-dismiss" -> "alert")
    }
  })
}
