package com.fastscala.db.observable

import com.fastscala.db.RowWithIdBase

trait ObservableRowBase extends RowWithIdBase {

  def saveX()(implicit obs: DBObserver): Any

  def deleteX()(implicit obs: DBObserver): Unit
}


