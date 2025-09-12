package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.bootstrap5.helpers.BSHelpers.RichElemBootstrapClasses
import com.fastscala.components.bootstrap5.helpers.ClassEnrichableMutable
import com.fastscala.core.FSContext

import scala.xml.{Elem, NodeSeq}

trait Table6StandardColumns extends Table6ColsRenderable with Table6ColsLabeled with Table6StdColsHelper {

  type C <: Table6StandardColumn[R, C, Ctx]

  def renderTableHeadTrTh()(implicit
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

  def renderTableBodyTrTd()(implicit
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

  def renderTableFootTrTh()(implicit
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

trait Table6StandardColumn[R, C <: Table6StandardColumn[R, C, Ctx], Ctx] extends ClassEnrichableMutable {
  self: C =>

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
  type C <: Table6StandardColumn[R, C, Ctx]
  type Ctx
  
  def ColStr(title: String, render: R => String) = new Table6StandardColumn[R, C, Ctx] {

    override def label: String = title

    override def renderTheadTrTh()(implicit tableHeadRerenderer: TableHeadRerenderer, trRerenderer: TrRerenderer, thRerenderer: ThRerenderer, colIdx: TableColIdx, pageRows: Seq[(String, R)], fsc: FSContext): Elem =
      <th>{title}</th>

    override def renderTbodyTrTd()(implicit tableBodyRerenderer: TableBodyRerenderer, trRerenderer: TrRerenderer, tdRerenderer: TdRerenderer, value: R, rowIdx: TableRowIdx, colIdx: TableColIdx, rows: Seq[(String, R)], fsc: FSContext): Elem =
      <td class={additionalClasses}>{render(value)}</td>
  }

  def ColNs(title: String, render: FSContext => R => NodeSeq) = new Table6StandardColumn[R, C, Ctx] {

    override def label: String = title

    override def renderTheadTrTh()(implicit tableHeadRerenderer: TableHeadRerenderer, trRerenderer: TrRerenderer, thRerenderer: ThRerenderer, colIdx: TableColIdx, pageRows: Seq[(String, R)], fsc: FSContext): Elem =
      <th>{title}</th>

    override def renderTbodyTrTd()(implicit tableBodyRerenderer: TableBodyRerenderer, trRerenderer: TrRerenderer, tdRerenderer: TdRerenderer, value: R, rowIdx: TableRowIdx, colIdx: TableColIdx, rows: Seq[(String, R)], fsc: FSContext): Elem =
      <td class={additionalClasses}>{render(fsc)(value)}</td>
  }

  def ColNsFull(title: String, render: FSContext => (TableBodyRerenderer, TrRerenderer, TdRerenderer, R, TableRowIdx, TableColIdx, Seq[(String, R)]) => NodeSeq) = new Table6StandardColumn[R, C, Ctx] {

    override def label: String = title

    override def renderTheadTrTh()(implicit tableHeadRerenderer: TableHeadRerenderer, trRerenderer: TrRerenderer, thRerenderer: ThRerenderer, colIdx: TableColIdx, pageRows: Seq[(String, R)], fsc: FSContext): Elem =
      <th>{title}</th>

    override def renderTbodyTrTd()(implicit tableBodyRerenderer: TableBodyRerenderer, trRerenderer: TrRerenderer, tdRerenderer: TdRerenderer, value: R, rowIdx: TableRowIdx, colIdx: TableColIdx, rows: Seq[(String, R)], fsc: FSContext): Elem =
      <td class={additionalClasses}>{render(fsc)(tableBodyRerenderer, trRerenderer, tdRerenderer, value, rowIdx, colIdx, rows)}</td>
  }

  def ColNsFullTd(title: String, render: FSContext => (TableBodyRerenderer, TrRerenderer, TdRerenderer, R, TableRowIdx, TableColIdx, Seq[(String, R)]) => Elem) = new Table6StandardColumn[R, C, Ctx] {

    override def label: String = title

    override def renderTheadTrTh()(implicit tableHeadRerenderer: TableHeadRerenderer, trRerenderer: TrRerenderer, thRerenderer: ThRerenderer, colIdx: TableColIdx, pageRows: Seq[(String, R)], fsc: FSContext): Elem =
      <th>{title}</th>

    override def renderTbodyTrTd()(implicit tableBodyRerenderer: TableBodyRerenderer, trRerenderer: TrRerenderer, tdRerenderer: TdRerenderer, value: R, rowIdx: TableRowIdx, colIdx: TableColIdx, rows: Seq[(String, R)], fsc: FSContext): Elem =
      render(fsc)(tableBodyRerenderer, trRerenderer, tdRerenderer, value, rowIdx, colIdx, rows).withClass(additionalClasses)
  }
}
