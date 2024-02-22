package com.fastscala.db

import scalikejdbc._

trait RowBase {

  def table: TableBase[_]

  def insert(): Unit
}

trait Row[R <: Row[R]] extends RowBase {
  self: R =>

  def table: TableBase[R]

  def insertSQL(): SQL[Nothing, NoExtractor] = table.insertSQL(this)

  def insert(): Unit = DB.localTx({ implicit session => insertSQL().execute() })

  def copyFrom(row: R): Unit = table.copyRow(row, this)
}
