package com.fastscala.templates.bootstrap5.tables

trait Table5SeqDataSource extends Table5Base {

  def seqRowsSource: Seq[R]

  override def rows(hints: Seq[RowsHint]): Seq[R] = seqRowsSource
}
