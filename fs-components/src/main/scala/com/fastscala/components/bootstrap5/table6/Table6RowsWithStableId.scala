package com.fastscala.components.bootstrap5.table6

trait Table6RowsWithStableId extends Table6Base {

  /** The row's id: from the table's id and [[getIdForRow]], so it's stable and unique in the page even with several
   * tables showing the same rows. */
  override def idForRow(row: R, rowIdx: Int): String = s"${tableId}-row-${getIdForRow(row)}"

  def getIdForRow(row: R): String
}
