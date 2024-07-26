package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.FSXmlEnv

import scala.util.chaining.scalaUtilChainingOps

trait Table5SeqSortableDataSource[E <: FSXmlEnv] extends Table5Base[E] with Table5Sortable[E] {

  def seqRowsSource: Seq[R]

  def rowsSorter: PartialFunction[C, Seq[R] => Seq[R]]

  def isSortable(col: C): Boolean = rowsSorter.isDefinedAt(col)

  override def rows(hints: Seq[RowsHint]): Seq[R] = {
    seqRowsSource.pipe(rows => {
      hints.collectFirst({ case hint: SortingRowsHint[C] => hint }).collect({
        case SortingRowsHint(sortCol: C, ascending) if rowsSorter.isDefinedAt(sortCol) =>
          val sorted = rowsSorter.apply(sortCol)(rows)
          if (ascending) sorted else sorted.reverse
      }).getOrElse(rows)
    }).pipe(rows => {
      hints.collectFirst({ case hint: PagingRowsHint => hint }).map({
        case PagingRowsHint(offset, limit) => rows.drop(offset.toInt).take(limit.toInt)
      }).getOrElse(rows)
    })
  }
}
