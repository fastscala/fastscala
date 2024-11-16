package com.fastscala.demo.components

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.templates.bootstrap5.modals.{BSModal5Base, BSModal5Size}
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.utils.IdGen
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.NodeSeq

trait LoggerUI {

  def title: String

  def log(mesg: String): Unit

  def info(mesg: String): Unit

  def success(mesg: String): Unit

  def danger(mesg: String): Unit

  def warn(mesg: String): Unit

  def debug(mesg: String): Unit

  def continue_?(): Boolean

  def finished(): Unit
}

class LoggerUISysoutOnly(val title: String) extends LoggerUI {

  override def log(mesg: String): Unit = println(mesg)

  override def info(mesg: String): Unit = println(mesg)

  override def success(mesg: String): Unit = println(mesg)

  override def danger(mesg: String): Unit = println(mesg)

  override def warn(mesg: String): Unit = println(mesg)

  override def debug(mesg: String): Unit = println(mesg)

  def continue_?(): Boolean = true

  def finished(): Unit = JS.void
}

class LoggerUIImpl(val title: String)(implicit fsc: FSContext) extends LoggerUI {

  import com.fastscala.templates.bootstrap5.helpers.BSHelpers.*

  val loggerOutputId = IdGen.id
  private var continue: Boolean = true
  private var hasFinished: Boolean = false

  def log(mesg: String): Unit = fsc.sendToPage(JS.prepend2(loggerOutputId, <pre style="white-space: pre-wrap; margin: 0;">{mesg}</pre>))

  def info(mesg: String): Unit = fsc.sendToPage(JS.prepend2(loggerOutputId, <pre style="white-space: pre-wrap; margin: 0;" class="text-info">{mesg}</pre>))

  def success(mesg: String): Unit = fsc.sendToPage(JS.prepend2(loggerOutputId, <pre style="white-space: pre-wrap; margin: 0;" class="text-success">{mesg}</pre>))

  def danger(mesg: String): Unit = fsc.sendToPage(JS.prepend2(loggerOutputId, <pre style="white-space: pre-wrap; margin: 0;" class="text-danger">{mesg}</pre>))

  def warn(mesg: String): Unit = fsc.sendToPage(JS.prepend2(loggerOutputId, <pre style="white-space: pre-wrap; margin: 0;" class="text-warning">{mesg}</pre>))

  def debug(mesg: String): Unit = fsc.sendToPage(JS.prepend2(loggerOutputId, <pre style="white-space: pre-wrap; margin: 0;" class="text-black-50">{mesg}</pre>))

  override def continue_?(): Boolean = continue

  private val modal = new BSModal5Base {
    override def modalHeaderTitle: String = title

    override def modalSize: BSModal5Size.Value = BSModal5Size.LG

    override def modalBodyContents()(implicit fsc: FSContext): NodeSeq = {
      div.withId(loggerOutputId).withStyle("min-height: 400px; background: #f5f5f5; max-height: 800px; overflow: auto;").apply {
        ""
      }
    }

    override def modalFooterContents()(implicit fsc: FSContext): Option[NodeSeq] = Some {
      if (hasFinished) {
        BSBtn().BtnSecondary.lbl("Close").onclick(hideAndRemoveAndDeleteContext()).btn
      } else {
        val btn: BSBtn = BSBtn().BtnDark.lbl("Stop").withRandomId
        btn.ajax(implicit fsc => {
          continue = false
          btn.disable()
        }).btn
      }
    }
  }

  def openProgressModal(): Js = fsc.page.initWebSocket() & modal.installAndShow()

  override def finished(): Unit = {
    hasFinished = true
    fsc.sendToPage(modal.rerenderModalFooterContent())
  }
}

object LoggerUI {

  implicit val Default: com.fastscala.demo.components.LoggerUISysoutOnly = new LoggerUISysoutOnly("Default")

  def runInSeparateThreadAndOpenProgressModal(title: String)(code: LoggerUI => Unit)(implicit fsc: FSContext): Js = {
    val loggerUI = new LoggerUIImpl(title)
    new Thread() {
      override def run(): Unit = code(loggerUI)
    }.start()
    loggerUI.openProgressModal()
  }
}