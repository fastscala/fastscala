package com.fastscala.components.bootstrap5.table5

import com.fastscala.core.FSContext

import scala.xml.Elem

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
