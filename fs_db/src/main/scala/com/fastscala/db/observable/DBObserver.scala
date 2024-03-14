package com.fastscala.db.observable

import com.fastscala.db.{RowBase, TableBase}

trait DBObserver {
  def observingTables: Seq[TableBase]

  def saved(table: TableBase, row: RowBase): Unit

  def deleted(table: TableBase, row: RowBase): Unit
}
