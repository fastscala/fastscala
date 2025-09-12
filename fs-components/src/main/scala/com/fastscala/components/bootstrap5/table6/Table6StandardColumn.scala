package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.bootstrap5.helpers.BSHelpers.RichElemBootstrapClasses
import com.fastscala.components.bootstrap5.helpers.ClassEnrichableMutable
import com.fastscala.core.FSContext

import scala.xml.{Elem, NodeSeq}

trait Table6StandardColumns extends Table6ColsRenderable with Table6ColsLabeled with Table6StdColsHelper {

  type C = Table6StandardColumn[R, Ctx]

  override def renderTableHeadTrTh()(implicit
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
  ): Elem = col.renderTheadTrTh()

  override def renderTableBodyTrTd()(implicit
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
  ): Elem = col.renderTbodyTrTd()

  override def renderTableFootTrTh()(implicit
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
  ): Elem = col.renderTfootTrTh()

  override def colLabel(col: C): String = col.label
}

trait Table6StandardColumn[R, Ctx] extends ClassEnrichableMutable {

  type C = Table6StandardColumn[R, Ctx]
  
  var additionalClasses = ""

  override def addClass(clas: String): this.type = {
    additionalClasses += s" $clas"
    this
  }

  def label: String

  def renderTheadTrTh()(implicit
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

  def renderTbodyTrTd()(implicit
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

  def renderTfootTrTh()(implicit
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

trait Table6StdColsHelper extends Table6Base {

  type R
  type C = Table6StandardColumn[R, Ctx]
  type Ctx
  
  def ColStr(title: String, render: R => String) = new Table6StandardColumn[R, Ctx] {

    override def label: String = title

    override def renderTheadTrTh()(implicit
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
    ): Elem = <th>{title}</th>

    override def renderTbodyTrTd()(implicit
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
    ): Elem = <td class={additionalClasses}>{render(row)}</td>

    override def renderTfootTrTh()(implicit
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
    ): Elem = <th></th>
  }

  def ColNs(title: String, render: FSContext => R => NodeSeq) = new Table6StandardColumn[R, Ctx] {

    override def label: String = title

    override def renderTheadTrTh()(implicit
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
    ): Elem = <th>{title}</th>

    override def renderTbodyTrTd()(implicit
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
    ): Elem = <td class={additionalClasses}>{render(fsc)(row)}</td>

    override def renderTfootTrTh()(implicit
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
    ): Elem = <th></th>
  }

  def ColNsFull(title: String, render: FSContext => (Ctx, Seq[(String, R)], Seq[(String, C)], TableWrapperRerenderer, TableRerenderer, TableHeadRerenderer, TableBodyRerenderer, TableFootRerenderer, TrRerenderer, TdRerenderer, C, TableColIdx, TableColId, R, TableRowIdx, TableRowId) => NodeSeq) = new Table6StandardColumn[R, Ctx] {

    override def label: String = title

    override def renderTheadTrTh()(implicit
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
    ): Elem = <th>{title}</th>

    override def renderTbodyTrTd()(implicit
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
    ): Elem = <td class={additionalClasses}>{render(fsc)(ctx, rowsWithIds, columnsWithIds, tableWrapperRenderer, tableRenderer, tableHeadRerenderer, tableBodyRerenderer, tableFootRerenderer, trRerenderer, tdRerenderer, col, colIdx, colId, row, rowIdx, rowId)}</td>

    override def renderTfootTrTh()(implicit
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
    ): Elem = <th></th>
  }

  def ColNsFullTd(title: String, render: FSContext => (Ctx, Seq[(String, R)], Seq[(String, C)], TableWrapperRerenderer, TableRerenderer, TableHeadRerenderer, TableBodyRerenderer, TableFootRerenderer, TrRerenderer, TdRerenderer, C, TableColIdx, TableColId, R, TableRowIdx, TableRowId) => Elem) = new Table6StandardColumn[R, Ctx] {

    override def label: String = title

    override def renderTheadTrTh()(implicit
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
    ): Elem = <th>{title}</th>

    override def renderTbodyTrTd()(implicit
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
    ): Elem = render(fsc)(ctx, rowsWithIds, columnsWithIds, tableWrapperRenderer, tableRenderer, tableHeadRerenderer, tableBodyRerenderer, tableFootRerenderer, trRerenderer, tdRerenderer, col, colIdx, colId, row, rowIdx, rowId).withClass(additionalClasses)

    override def renderTfootTrTh()(implicit
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
    ): Elem = <th></th>
  }
}
