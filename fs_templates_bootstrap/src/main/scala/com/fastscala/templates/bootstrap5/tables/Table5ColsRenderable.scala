package com.fastscala.templates.bootstrap5.tables

import com.fastscala.code.FSContext

import scala.xml.{Elem, NodeSeq}

trait Table5ColsRenderable {

  type R
  type C

  def renderTRTD()(
    implicit tableBodyRerenderer: TableBodyRerenderer,
    trRerenderer: TRRerenderer,
    tdRerenderer: TDRerenderer,
    value: R,
    rowIdx: TableRowIdx,
    columns: Seq[(String, C)],
    rows: Seq[(String, R)],
    colThId: String,
    col: C,
    tableColIdx: TableColIdx,
    fsc: FSContext
  ): Elem

  def renderTableHeadTRTH()(
    implicit tableHeadRerenderer: TableHeadRerenderer,
    trRerenderer: TRRerenderer,
    thRerenderer: THRerenderer,
    columns: Seq[(String, C)],
    rows: Seq[(String, R)],
    colThId: String,
    col: C,
    tableColIdx: TableColIdx,
    fsc: FSContext
  ): Elem
}
