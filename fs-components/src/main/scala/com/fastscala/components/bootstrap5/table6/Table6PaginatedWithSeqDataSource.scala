package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.bootstrap5.helpers.BSHelpers.RichElemBootstrapClasses
import com.fastscala.components.bootstrap5.utils.{BSBtn, ImmediateInputFields}
import com.fastscala.core.FSContext
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromElems
import com.fastscala.utils.Lazy

import scala.xml.{Elem, NodeSeq}

trait Table6PaginatedWithSeqDataSource extends Table6Paginated with Table6SeqDataSource {

  override def knownTotalNumberOfRows: Option[Int] = Some(seqRowsSource.size)
}
