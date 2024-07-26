package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.{FSContext, FSXmlEnv}

trait Table5ColsRenderable[E <: FSXmlEnv] {

  type R
  type C

  def renderTRTD()(
    implicit tableBodyRerenderer: TableBodyRerenderer[E],
    trRerenderer: TRRerenderer[E],
    tdRerenderer: TDRerenderer[E],
    value: R,
    rowIdx: TableRowIdx,
    columns: Seq[(String, C)],
    rows: Seq[(String, R)],
    colThId: String,
    col: C,
    tableColIdx: TableColIdx,
    fsc: FSContext
  ): E#Elem

  def renderTableHeadTRTH()(
    implicit tableHeadRerenderer: TableHeadRerenderer[E],
    trRerenderer: TRRerenderer[E],
    thRerenderer: THRerenderer[E],
    columns: Seq[(String, C)],
    rows: Seq[(String, R)],
    colThId: String,
    col: C,
    tableColIdx: TableColIdx,
    fsc: FSContext
  ): E#Elem
}
