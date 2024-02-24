package com.fastscala.db

import scalikejdbc._

trait Row[R <: Row[R]] extends RowBase {
  self: R =>

  def table: Table[R]

  def insertSQL(): SQL[Nothing, NoExtractor] = table.insertSQL(this)

  def insert(): Unit = DB.localTx({ implicit session => insertSQL().execute() })

  def copyFrom(row: R): Unit = table.copyRow(row, this)
}
