package com.fastscala.utils

import com.fastscala.core.FSSystem
import com.fastscala.stats.StatEvent

object Missing extends Enumeration {

  val Session, Page, AnonPage, CallbackFunction, FileDownloadFunction, FileUploadFunction = Value

  implicit class RichValue(val v: Value) extends AnyVal {
    def updateStats()(implicit fss: FSSystem): Unit = v match {
      case Session =>
        fss.stats.sessionNotFoundTotal.inc()
        if (fss.eventsEnableNotFoundSession) fss.stats.event(StatEvent.NOT_FOUND_SESSION)
      case Page =>
        fss.stats.pageNotFoundTotal.inc()
        if (fss.eventsEnableNotFoundPage) fss.stats.event(StatEvent.NOT_FOUND_PAGE)
      case AnonPage =>
        fss.stats.anonPageNotFoundTotal.inc()
        if (fss.eventsEnableNotFoundAnonPage) fss.stats.event(StatEvent.NOT_FOUND_ANON_PAGE)
      case CallbackFunction =>
        fss.stats.callbackNotFoundTotal.inc()
        if (fss.eventsEnableNotFoundCallback) fss.stats.event(StatEvent.NOT_FOUND_CALLBACK)
      case FileDownloadFunction =>
        fss.stats.fileDownloadCallbackNotFoundTotal.inc()
        if (fss.eventsEnableNotFoundFileDownload) fss.stats.event(StatEvent.NOT_FOUND_FILE_DOWNLOAD)
      case FileUploadFunction =>
        fss.stats.fileUploadCallbackNotFoundTotal.inc()
        if (fss.eventsEnableNotFoundFileUpload) fss.stats.event(StatEvent.NOT_FOUND_FILE_UPLOAD)
    }
  }
}
