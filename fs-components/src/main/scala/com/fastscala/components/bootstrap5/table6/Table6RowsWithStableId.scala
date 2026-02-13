package com.fastscala.components.bootstrap5.table6

trait Table6RowsWithStableId extends Table6Base {

  override def idForRow(row: R, rowIdx: Int): String = "row-" + getIdForRow(row)

  def getIdForRow(row: R): String
}
