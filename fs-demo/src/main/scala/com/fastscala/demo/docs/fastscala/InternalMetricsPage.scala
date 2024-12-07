package com.fastscala.demo.docs.fastscala

import cats.effect.IO
import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.bootstrap5.progress.BSProgress
import com.fastscala.components.bootstrap5.utils.BSBtn
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import java.text.DecimalFormat

class InternalMetricsPage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Internal Metrics"

  override def openWSSessionAtStart: Boolean = true

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderSnippet("Source") {
      import com.fastscala.components.bootstrap5.helpers.BSHelpers.*
      val rerenderable = JS.rerenderable(rerenderer => implicit fsc => {
        val totalMem = Runtime.getRuntime.totalMemory()
        val freeMem = Runtime.getRuntime.freeMemory()
        val usedMem = totalMem - freeMem
        div.apply {
        <div>
          {
          BSProgress().striped.animated.bgWarning.renderXofY(usedMem, totalMem, readableSize(usedMem)).mt_1.mb_2
          }
        </div>
        <ul>
          <li><b>Total Memory:</b> {readableSize(totalMem)}</li>
          <li><b>Free Memory:</b> {readableSize(freeMem)}</li>
          <li><b>#GC runs:</b> {fsc.session.fsSystem.stats.gcRunsTotal.get().toLong}</li>
          <li><b>Total GC time:</b> {fsc.session.fsSystem.stats.gcTimeTotal.get().formatted("%.3f")}s</li>
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
            BSBtn().BtnPrimary.lbl("System GC").ajax(_ => {
              fsc.page.session.fsSystem.freeUpSpace()
              JS.void
            }).btn.me_2 ++
              BSBtn().BtnPrimary.lbl("Free Space").ajax(_ => {
                fsc.page.session.fsSystem.freeUpSpace()
                JS.void
              }).btn
          }
      }
    }
    closeSnippet()
  }

  def readableSize(size: Long): String = {
    if (size <= 0) return "0"
    val units = Array("B", "kB", "MB", "GB", "TB", "PB", "EB")
    val digitGroups = (Math.log10(size) / Math.log10(1024)).toInt
    new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units(digitGroups)
  }
}
