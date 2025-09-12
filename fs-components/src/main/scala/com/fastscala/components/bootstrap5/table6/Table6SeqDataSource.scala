package com.fastscala.components.bootstrap5.table6

import scala.util.chaining.scalaUtilChainingOps

trait Table6SeqDataSource extends Table6Base {

  type Ctx = Null
  def seqRowsSource: Seq[R]

  override def rows(hints: Seq[RowsHint]): (Seq[R], Ctx) = (seqRowsSource.pipe(rows => {
    hints.collectFirst({ case hint: PagingRowsHint => hint }).map({
      case PagingRowsHint(offset, limit) => rows.drop(offset.toInt).take(limit.toInt)
    }).getOrElse(rows)
  }), null)
}
