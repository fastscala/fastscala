package com.fastscala.db.observable

import com.fastscala.db.{RowWithId, RowWithIdBase}

trait ObservableRow[K, R <: ObservableRow[K, R]] extends RowWithId[K, R] with ObservableRowBase with RowWithIdBase {
  self: R =>

  override def saveX()(implicit obs: DBObserver): R = try {
    obs.beforeSaved(table, this)
    save()
  } finally {
    obs.saved(table, this)
  }

  override def deleteX()(implicit obs: DBObserver): Unit = try {
    obs.beforeDelete(table, this)
    delete()
  } finally {
    obs.deleted(table, this)
  }
}
