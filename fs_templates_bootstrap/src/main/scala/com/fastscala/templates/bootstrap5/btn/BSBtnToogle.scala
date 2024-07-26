package com.fastscala.templates.bootstrap5.components

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.utils.BSBtn

import scala.util.chaining.scalaUtilChainingOps

object BSBtnToogle {

  implicit class RichBSBtnToogler[E <: FSXmlEnv : FSXmlSupport](btn: BSBtn[E]) {

    def toggler(
                 get: () => Boolean,
                 set: Boolean => Js,
                 falseLbl: String,
                 trueLbl: String,
                 falseTransform: BSBtn[E] => BSBtn[E] = identity[BSBtn[E]],
                 trueTransform: BSBtn[E] => BSBtn[E] = identity[BSBtn[E]]
               )(implicit fsc: FSContext): E#Elem = {
      var current = get()
      Js.rerenderable[E](rerenderer => implicit fsc => {
        btn.lbl(if (current) trueLbl else falseLbl)
          .ajax(implicit fsc => {
            current = !current
            set(current) & rerenderer.rerender()
          }).pipe(if (current) trueTransform else falseTransform).btn
      }).render()
    }
  }
}
