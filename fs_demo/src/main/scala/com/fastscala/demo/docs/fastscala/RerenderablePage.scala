package com.fastscala.demo.docs.fastscala

import com.fastscala.core.FSContext
import com.fastscala.demo.db.User
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.utils.BSBtn
import io.circe.Decoder
import io.circe.generic.semiauto

import java.util.Date

class RerenderablePage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "FastScala Rerenderable"

  override def renderExamples()(implicit fsc: FSContext): Unit = {
    import com.fastscala.templates.bootstrap5.classes.BSHelpers._

    renderSnippet("Rerenderable from outside") {
      val rerenderer = Js.rerenderableContents(rerenderer => implicit fsc => {
        div.withStyle(s"height: 100px; width: 300px;background-color: rgb(${math.random() * 255},${math.random() * 255},${math.random() * 255})").rounded_1.rounded.mx_auto.p_2.my_2.apply(s"Time on server: ${new Date().toString}")
      })
      rerenderer.render() ++
        BSBtn.BtnPrimary.lbl("Re-render").ajax(_ => rerenderer.rerender()).btn.w_100
    }
    renderSnippet("Rerenderable from inside the block") {
      Js.rerenderableContents(rerenderer => implicit fsc => {
        div.withStyle(s"height: 100px;background-color: rgb(${math.random() * 255},${math.random() * 255},${math.random() * 255})").rounded_1.rounded.mx_auto.p_2.my_2.apply(
          p.apply(s"Time on server: ${new Date().toString}").mb_2 ++
            BSBtn.BtnSuccess.lbl("Re-render").ajax(_ => rerenderer.rerender()).btn.w_100
        )

      }).render()
    }
    renderSnippet("Rerenderable with arguments") {
      val rerenderer = Js.rerenderableContentsP[Option[Int]](rerenderer => implicit fsc => pointsOpt => {
        div.text_center.fs_1.fw_bolder.withStyle(s"height: 100px;background-color: rgb(${math.random() * 255},${math.random() * 255},${math.random() * 255})").rounded_1.rounded.mx_auto.p_2.my_2.apply(
          pointsOpt.map(_.toString).getOrElse("--")
        )
      })
      rerenderer.render(None) ++
        d_flex.justify_content_between.apply {
          BSBtn.BtnSuccess.lbl("Undefined").ajax(_ => rerenderer.rerender(None)).btn ++
            BSBtn.BtnSuccess.lbl("5pts").ajax(_ => rerenderer.rerender(Some(5))).btn ++
            BSBtn.BtnSuccess.lbl("20pts").ajax(_ => rerenderer.rerender(Some(20))).btn ++
            BSBtn.BtnSuccess.lbl("100pts").ajax(_ => rerenderer.rerender(Some(100))).btn
        }
    }
    closeSnippet()
  }
}
