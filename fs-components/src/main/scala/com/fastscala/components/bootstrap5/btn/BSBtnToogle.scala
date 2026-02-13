package com.fastscala.components.bootstrap5.components

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.bootstrap5.utils.BSBtn
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem

object BSBtnToogle {

  implicit class RichBSBtnToogler(val btn: BSBtn) extends AnyVal {

    def toggler(
                 get: () => Boolean,
                 set: Boolean => Js,
                 falseLbl: String,
                 trueLbl: String,
                 falseTransform: BSBtn => BSBtn = identity[BSBtn],
                 trueTransform: BSBtn => BSBtn = identity[BSBtn]
               )(implicit fsc: FSContext): Elem = {
      var current = get()
      JS.rerenderable(rerenderer => implicit fsc => {
        btn.lbl(if (current) trueLbl else falseLbl)
          .callback(implicit fsc => {
            current = !current
            set(current) & rerenderer.rerender()
          }).pipe(if (current) trueTransform else falseTransform).btn
      }).render()
    }
  }
}
