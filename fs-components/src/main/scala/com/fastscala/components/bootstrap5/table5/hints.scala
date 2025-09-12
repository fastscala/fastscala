package com.fastscala.components.bootstrap5.table5

trait RowsHint

case class PagingRowsHint(offset: Long, limit: Long) extends RowsHint

case class SortingRowsHint[C](sortCol: C, ascending: Boolean) extends RowsHint

trait THRenderHint

case class SortingTHRenderHint(ascending: Boolean) extends RowsHint
