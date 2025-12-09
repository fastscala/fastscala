package com.fastscala.components.bootstrap5.table6

trait Table6RowsWithId extends Table6Base {

  override def idForRow(row: R, rowIdx: Int): String = "row-" + getIdForRow(row)

  def getIdForRow(row: R): String

  def getRowForId(id: String): Option[R]
}
