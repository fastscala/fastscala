package com.fastscala.templates.bootstrap5.tables

import scala.util.chaining.scalaUtilChainingOps

trait Table5SeqDataSource extends Table5Base {

  def seqRowsSource: Seq[R]

  override def rows(hints: Seq[RowsHint]): Seq[R] = seqRowsSource
}
