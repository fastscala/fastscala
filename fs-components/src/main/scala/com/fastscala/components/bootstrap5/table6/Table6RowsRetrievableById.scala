package com.fastscala.components.bootstrap5.table6

trait Table6RowsRetrievableById extends Table6RowsWithStableId {

  def getRowForId(id: String): Option[R]
}
