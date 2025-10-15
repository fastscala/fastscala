package com.fastscala.components.bootstrap5.table6

trait Table6RowsWithId extends Table6Base {

  def getIdForRow(row: R): String

  def getRowForId(id: String): Option[R]
}
