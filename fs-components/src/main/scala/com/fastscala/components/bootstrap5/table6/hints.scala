package com.fastscala.components.bootstrap5.table6

trait RowsHint

case class PagingRowsHint(offset: Long, limit: Long) extends RowsHint

case class SortingRowsHint(sortCol: Any, ascending: Boolean) extends RowsHint

trait THRenderHint

case class SortingTHRenderHint(ascending: Boolean) extends RowsHint
