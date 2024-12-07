package com.fastscala.db.observable

import com.fastscala.db.{RowBase, TableBase}

trait DBObserver {
  def observingTables: Seq[TableBase]

  def preSave(table: TableBase, row: RowBase): Unit

  def postSave(table: TableBase, row: RowBase): Unit

  def preDelete(table: TableBase, row: RowBase): Unit

  def postDelete(table: TableBase, row: RowBase): Unit
}

object DBObserver {
  implicit val NilObserver: DBObserver = new DBObserver {
    override def observingTables: Seq[TableBase] = Seq()

    override def preSave(table: TableBase, row: RowBase): Unit = ()

    override def postSave(table: TableBase, row: RowBase): Unit = ()

    override def preDelete(table: TableBase, row: RowBase): Unit = ()

    override def postDelete(table: TableBase, row: RowBase): Unit = ()
  }
}