package com.fastscala.db

import scalikejdbc._

trait RowBase {

  def table: Table[_]

  def insert(): Unit

  def isPersisted_?(): Boolean
}

trait Row[R <: Row[R]] extends RowBase {
  self: R =>

  def table: Table[R]

  def insertSQL(): SQL[Nothing, NoExtractor] = table.insertSQL(this)

  def insert(): Unit = DB.localTx({ implicit session => insertSQL().execute()() })
}
