package com.fastscala.templates.bootstrap5.tables

trait RowsHint

case class PagingRowsHint(offset: Long, limit: Long) extends RowsHint

case class SortingRowsHint[C](sortCol: C, ascending: Boolean) extends RowsHint

trait THRenderHint

case class SortingTHRenderHint(ascending: Boolean) extends RowsHint
