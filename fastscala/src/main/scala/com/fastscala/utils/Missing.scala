package com.fastscala.utils

import com.fastscala.core.FSSystem
import com.fastscala.stats.StatEvent

object Missing extends Enumeration {

  val Session, Page, AnonPage, CallbackFunction, FileDownloadFunction, FileUploadFunction = Value

  implicit class RichValue(v: Value) {
    def updateStats()(implicit fss: FSSystem): Unit = v match {
      case Session =>
        fss.stats.sessionNotFoundTotal.inc()
        fss.stats.event(StatEvent.NOT_FOUND_SESSION)
      case Page =>
        fss.stats.pageNotFoundTotal.inc()
        fss.stats.event(StatEvent.NOT_FOUND_PAGE)
      case AnonPage =>
        fss.stats.anonPageNotFoundTotal.inc()
        fss.stats.event(StatEvent.NOT_FOUND_ANON_PAGE)
      case CallbackFunction =>
        fss.stats.callbackNotFoundTotal.inc()
        fss.stats.event(StatEvent.NOT_FOUND_CALLBACK)
      case FileDownloadFunction =>
        fss.stats.fileDownloadCallbackNotFoundTotal.inc()
        fss.stats.event(StatEvent.NOT_FOUND_FILE_DOWNLOAD)
      case FileUploadFunction =>
        fss.stats.fileUploadCallbackNotFoundTotal.inc()
        fss.stats.event(StatEvent.NOT_FOUND_FILE_UPLOAD)
    }
  }
}
