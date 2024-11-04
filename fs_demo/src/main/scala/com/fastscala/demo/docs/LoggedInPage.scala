package com.fastscala.demo.docs

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.xml.scala_xml.JS

import java.time.LocalDate
import scala.xml.NodeSeq

abstract class LoggedInPage() extends BasePage {

  implicit val atTime: LocalDate = LocalDate.now()

  override def navBarTopRight()(implicit fsc: FSContext): NodeSeq =
    <div class="text-end justify-content-end">
        <button class="btn btn-secondary me-2">#Callbacks: <span id="fs_num_page_callbacks">--</span></button>
        <a href="https://training.fastscala.com/" class="btn btn-warning">Register for Free Training!</a>
    </div>
  //      <div class="text-end justify-content-end ms-2">
  //        <a href="https://github.com/fastscala/fastscala" class="btn btn-warning">GitHub</a>
  //    </div> // ++
  //      BSBtn().BtnOutlineWarning.lbl("Logout").ajax(implicit fsc => {
  //        user.logOut()
  //      }).btn.ms_2

  def renderSideMenu()(implicit fsc: FSContext): NodeSeq = {
    FSDemoMainMenu.render() // ++
    //      hr ++
    //      p_3.d_flex.align_items_center.apply {
    //        a.apply(user.miniHeadshotOrPlaceholderRendered.withStyle("width: 55px;border-radius: 55px;")) ++
    //          a.text_decoration_none.fw_bold.text_warning.ms_2.apply(user.fullName)
    //      }
  }

  lazy val pageRenderer = JS.rerenderableContents(rerenderer => implicit fsc => {
    renderPageContents()
  })

  def rerenderPageContents(): Js = pageRenderer.rerender()

  def renderPageContents()(implicit fsc: FSContext): NodeSeq
}
