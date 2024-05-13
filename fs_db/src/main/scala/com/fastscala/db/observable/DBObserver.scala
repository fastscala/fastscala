package com.fastscala.db.observable

import com.fastscala.db.{RowBase, TableBase}

trait DBObserver {
  def observingTables: Seq[TableBase]

  def beforeSaved(table: TableBase, row: RowBase): Unit

  def saved(table: TableBase, row: RowBase): Unit

  def beforeDelete(table: TableBase, row: RowBase): Unit

  def deleted(table: TableBase, row: RowBase): Unit
}

object DBObserver {
  implicit val NilObserver = new DBObserver {
    override def observingTables: Seq[TableBase] = Seq()

    override def beforeSaved(table: TableBase, row: RowBase): Unit = ()

    override def saved(table: TableBase, row: RowBase): Unit = ()

    override def beforeDelete(table: TableBase, row: RowBase): Unit = ()

    override def deleted(table: TableBase, row: RowBase): Unit = ()
  }
}