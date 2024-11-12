package com.fastscala.stats

import com.fastscala.core.{FSXmlElem, FSXmlNodeSeq, FSContext, FSPage, FSSession, FSSystem}
import com.github.loki4j.slf4j.marker.LabelMarker
import io.prometheus.metrics.core.metrics.{Counter, Gauge}
import org.apache.commons.text.StringEscapeUtils
import org.slf4j.event.Level
import org.slf4j.{Logger, LoggerFactory}

class FSStats(
               logger: Logger = LoggerFactory.getLogger("com.fastscala.stats.FSStats")
             ) {

  import StatEvent._

  val sessionsTotal = Counter.builder().name("fs_session_total").help("total number of sessions created since application was started").register()
  val pagesTotal = Counter.builder().name("fs_page_total").help("total number of pages created since application was started").register()
  val anonPagesTotal = Counter.builder().name("fs_anon_page_total").help("total number of anonymous pages created since application was started").register()
  val fileDownloadCallabacksTotal = Counter.builder().name("fs_file_download_callback_total").help("total number of file download handlers created since application was started").register()
  val fileUploadCallbacksTotal = Counter.builder().name("fs_file_upload_callback_total").help("total number of file upload handlers created since application was started").register()
  val callbacksTotal = Counter.builder().name("fs_callback_total").help("total number of callbacks created since application was started").register()

  val callbackInvocationsTotal = Counter.builder().name("fs_callback_invocations_total").help("total number of callbacks invocations since application was started").register()
  val callbackErrorsTotal = Counter.builder().name("fs_callback_errors_total").help("total number of callbacks invocations resulting in an internal error since application was started").register()
  val callbackTimeTotal = Counter.builder().name("fs_callback_time_seconds_total").unit(io.prometheus.metrics.model.snapshots.Unit.SECONDS).help("total time processing callback invocations since application was started").register()
  val callbacksInProcessing = Gauge.builder().name("fs_callback_being_processed").help("total number of callbacks being processed").register()

  val fileDownloadCallbackInvocationsTotal = Counter.builder().name("fs_file_download_callback_invocations_total").help("total number of file download callback invocations since application was started").register()
  val fileDownloadCallbackErrorsTotal = Counter.builder().name("fs_file_download_callback_errors_total").help("total number of file download callback invocations resulting in an internal error since application was started").register()
  val fileDownloadCallbackTimeTotal = Counter.builder().name("fs_file_download_callback_time_seconds_total").unit(io.prometheus.metrics.model.snapshots.Unit.SECONDS).help("total time processing file download callback invocations since application was started").register()
  val fileDownloadCallbacksInProcessing = Gauge.builder().name("fs_file_download_callback_being_processed").help("total number of file download callbacks being processed").register()

  val fileUploadCallbackInvocationsTotal = Counter.builder().name("fs_file_upload_callback_invocations_total").help("total number of file upload callback invocations since application was started").register()
  val fileUploadCallbackErrorsTotal = Counter.builder().name("fs_file_upload_callback_errors_total").help("total number of file upload callback invocations resulting in an internal error since application was started").register()
  val fileUploadCallbackTimeTotal = Counter.builder().name("fs_file_upload_callback_time_seconds_total").unit(io.prometheus.metrics.model.snapshots.Unit.SECONDS).help("total time processing file upload callback invocations since application was started").register()
  val fileUploadCallbacksInProcessing = Gauge.builder().name("fs_file_upload_callback_being_processed").help("total number of file upload callbacks being processed").register()

  val currentSessions = Gauge.builder().name("fs_current_num_sessions").help("current number of sessions").register()
  val currentPages = Gauge.builder().name("fs_current_num_pages").help("current number of pages").register()
  val currentAnonPages = Gauge.builder().name("fs_current_num_anon_pages").help("current number of anonymous pages").register()
  val currentCallbacks = Gauge.builder().name("fs_current_num_callbacks").help("current number of callbacks").register()
  val currentFileDownloadCallbacks = Gauge.builder().name("fs_current_num_file_download_callbacks").help("current number of file download handlers").register()
  val currentFileUploadCallbacks = Gauge.builder().name("fs_current_num_file_upload_callbacks").help("current number of file upload handlers").register()

  val sessionNotFoundTotal = Counter.builder().name("fs_session_not_found_total").help("total number of sessions not found since application was started").register()
  val pageNotFoundTotal = Counter.builder().name("fs_page_not_found_total").help("total number of pages not found since application was started").register()
  val anonPageNotFoundTotal = Counter.builder().name("fs_anon_page_not_found_total").help("total number of anonymous pages not found since application was started").register()
  val fileDownloadCallbackNotFoundTotal = Counter.builder().name("fs_file_download_callback_not_found_total").help("total number of file download handlers not found since application was started").register()
  val fileUploadCallbackNotFoundTotal = Counter.builder().name("fs_file_upload_callback_not_found_total").help("total number of file upload handlers not found since application was started").register()
  val callbackNotFoundTotal = Counter.builder().name("fs_callback_not_found_total").help("total number of callbacks not found since application was started").register()

  val keepAliveInvocationsTotal = Counter.builder().name("fs_keepalive_invocations_total").help("total number of keep alive invocations since application was started").register()
  val gcRunsTotal = Counter.builder().name("fs_gc_runs_total").help("total number of gc runs since application was started").register()
  val gcTimeTotal = Counter.builder().name("fs_gc_time_seconds_total").unit(io.prometheus.metrics.model.snapshots.Unit.SECONDS).help("total number of gc time since application was started").register()

  def eventLevel(event: StatEvent): Level = event match {
    case CREATE_CALLBACK | USE_CALLBACK | GC_CALLBACK => Level.TRACE
    case _ => Level.DEBUG
  }

  def event(
             event: StatEvent,
             n: Int = 1,
             additionalFields: Seq[(String, String)] = Nil
           )(implicit
             fsSystem: FSSystem
             , fsSessionOpt: Option[FSSession] = None
             , fsPageOpt: Option[FSPage] = None
             , fsContextOpt: Option[FSContext] = None
           ): Unit = {
    logger.atLevel(eventLevel(event))
      .addMarker(LabelMarker.of("app", () => fsSystem.appName))
      .log(List(
        List("event" -> event.name)
        , fsSessionOpt.toList.map(session => "session_id" -> session.id)
        , fsSessionOpt.toList.flatMap(_.debugLbl.map("session_lbl" -> _))
        , fsPageOpt.toList.map(page => "page_id" -> page.id)
        , fsPageOpt.toList.flatMap(_.debugLbl.map("page_lbl" -> _))
        , fsContextOpt.toList.map("context_lbl" -> _.fullPath)
        , additionalFields
      ).flatten.map({
        case (key, value) => s""""$key": ${StringEscapeUtils.escapeJson(value)}"""
      }).mkString("{", ",", "}"))
  }
}
