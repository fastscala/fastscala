package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.FSXmlEnv

trait Table5SeqDataSource[E <: FSXmlEnv] extends Table5Base[E] {

  def seqRowsSource: Seq[R]

  override def rows(hints: Seq[RowsHint]): Seq[R] = seqRowsSource
}
