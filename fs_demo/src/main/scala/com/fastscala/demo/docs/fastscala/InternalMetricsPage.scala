package com.fastscala.demo.docs.fastscala

import cats.effect.IO
import com.fastscala.core.FSContext
import com.fastscala.demo.docs.SingleCodeExamplePage
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.xml.scala_xml.JS

import java.text.DecimalFormat
import scala.xml.NodeSeq

class InternalMetricsPage extends SingleCodeExamplePage() {

  override def pageTitle: String = "Internal Metrics"

  override def openWSSessionAtStart: Boolean = true

  override def renderExampleContents()(implicit fsc: FSContext): NodeSeq = {
    // === code snippet ===
    import com.fastscala.templates.bootstrap5.helpers.BSHelpers._
    val rerenderable = JS.rerenderable(rerenderer => implicit fsc => {
      div.apply {
        <ul>
          <li><b>Total Memory:</b> {readableSize(Runtime.getRuntime.totalMemory())}</li>
          <li><b>Free Memory:</b> {readableSize(Runtime.getRuntime.freeMemory())}</li>
          <li><b>Total #Sessions:</b> {fsc.session.fsSystem.sessions.size}</li>
          <li><b>Total #Pages:</b> {fsc.session.fsSystem.sessions.map(_._2.pages.size).sum}</li>
          <li class="ms-2"><b>Total #Defunct Pages:</b> {fsc.session.fsSystem.sessions.map(_._2.pages.count(_._2.isDefunct_?)).sum}</li>
          <li><b>Total #Anonymous Pages:</b> {fsc.session.fsSystem.sessions.map(_._2.anonymousPages.size).sum}</li>
          <li><b>Total #Callback Functions:</b> {fsc.session.fsSystem.sessions.flatMap(_._2.pages.map(_._2.callbacks.size)).sum}</li>
          <li><b>Total #File Download Callback Functions:</b> {fsc.session.fsSystem.sessions.flatMap(_._2.pages.map(_._2.fileDownloadCallbacks.size)).sum}</li>
          <li><b>Total #File Upload Callback Functions:</b> {fsc.session.fsSystem.sessions.flatMap(_._2.pages.map(_._2.fileUploadCallbacks.size)).sum}</li>
        </ul>
          <h6>This session:</h6>
          <ul>
            <li><b>#Pages:</b> {fsc.session.pages.size}</li>
            <li class="ms-2"><b>#Defunct Pages:</b> {fsc.session.pages.count(_._2.isDefunct_?)}</li>
            <li><b>#Anonymous Pages:</b> {fsc.session.anonymousPages.size}</li>
            <li><b>#Callback Functions:</b> {fsc.session.pages.map(_._2.callbacks.size).sum}</li>
            <li><b>#File Download Callback Functions:</b> {fsc.session.pages.map(_._2.fileDownloadCallbacks.size).sum}</li>
            <li><b>#File Upload Callback Functions:</b> {fsc.session.pages.map(_._2.fileUploadCallbacks.size).sum}</li>
          </ul>
      }
    })

    import scala.concurrent.duration.DurationInt
    def updateMetrics(): IO[Unit] = for {
      _ <- IO.sleep(1000.millis)
      _ <- IO(fsc.sendToPage(rerenderable.rerender()))
      _ <- if (fsc.page.isDefunct_?) IO.unit else updateMetrics()
    } yield ()

    import cats.effect.unsafe.implicits.global
    updateMetrics().unsafeRunAndForget()
    border.p_2.rounded.apply {
      rerenderable.render() ++
        d_flex.justify_content_center.apply {
          BSBtn().BtnPrimary.lbl("Free Space").ajax(_ => {
            fsc.page.session.fsSystem.freeUpSpace()
            JS.void
          }).btn
        }
    }
    // === code snippet ===
  }

  def readableSize(size: Long): String = {
    if (size <= 0) return "0"
    val units = Array("B", "kB", "MB", "GB", "TB", "PB", "EB")
    val digitGroups = (Math.log10(size) / Math.log10(1024)).toInt
    new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units(digitGroups)
  }
}
