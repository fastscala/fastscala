package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.FSContext
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.utils.Lazy

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.NodeSeq

trait Table5SeqSortableDataSource extends Table5Base with Table5Sortable {

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
