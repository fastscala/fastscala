package com.fastscala.components.bootstrap5.table6

import com.fastscala.core.FSContext

import scala.xml.Elem

trait Table6ColsRenderable {

  type R
  type C
  type Ctx

  def renderTableHeadTrTh()(
    implicit
    fsc: FSContext,
    ctx: Ctx,
    rowsWithIds: Seq[(String, R)],
    columnsWithIds: Seq[(String, C)],
    tableWrapperRenderer: TableWrapperRerenderer,
    tableRenderer: TableRerenderer,
    tableHeadRerenderer: TableHeadRerenderer,
    tableBodyRerenderer: TableBodyRerenderer,
    tableFootRerenderer: TableFootRerenderer,
    trRerenderer: TrRerenderer,
    thRerenderer: ThRerenderer,
    col: C,
    colIdx: TableColIdx,
    colId: TableColId
  ): Elem

  def renderTableBodyTrTd()(
    implicit
    fsc: FSContext,
    ctx: Ctx,
    rowsWithIds: Seq[(String, R)],
    columnsWithIds: Seq[(String, C)],
    tableWrapperRenderer: TableWrapperRerenderer,
    tableRenderer: TableRerenderer,
    tableHeadRerenderer: TableHeadRerenderer,
    tableBodyRerenderer: TableBodyRerenderer,
    tableFootRerenderer: TableFootRerenderer,
    trRerenderer: TrRerenderer,
    tdRerenderer: TdRerenderer,
    col: C,
    colIdx: TableColIdx,
    colId: TableColId,
    row: R,
    rowIdx: TableRowIdx,
    rowId: TableRowId
  ): Elem

  def renderTableFootTrTh()(
    implicit
    fsc: FSContext,
    ctx: Ctx,
    rowsWithIds: Seq[(String, R)],
    columnsWithIds: Seq[(String, C)],
    tableWrapperRenderer: TableWrapperRerenderer,
    tableRenderer: TableRerenderer,
    tableHeadRerenderer: TableHeadRerenderer,
    tableBodyRerenderer: TableBodyRerenderer,
    tableFootRerenderer: TableFootRerenderer,
    trRerenderer: TrRerenderer,
    thRerenderer: ThRerenderer,
    col: C,
    colIdx: TableColIdx,
    colId: TableColId
  ): Elem

}
