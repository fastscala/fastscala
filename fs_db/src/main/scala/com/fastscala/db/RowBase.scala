package com.fastscala.db

trait RowBase {

  def table: TableBase

  def insert(): Unit
}
