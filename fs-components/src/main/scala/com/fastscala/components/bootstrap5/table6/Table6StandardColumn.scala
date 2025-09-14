package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.bootstrap5.helpers.BSHelpers.RichElemBootstrapClasses
import com.fastscala.components.bootstrap5.helpers.ClassEnrichableMutable
import com.fastscala.core.FSContext

import scala.xml.{Elem, NodeSeq}

trait Table6StandardColumns extends Table6ColsRenderable with Table6ColsLabeled with Table6StdColsHelper {

  type C = Table6StandardColumn[R]

  override def renderTableHeadTrTh()(implicit
      fsc: FSContext,
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

trait Table6StandardColumn[R] extends ClassEnrichableMutable {

  type C = Table6StandardColumn[R]

  var additionalClasses = ""

  override def addClass(clas: String): this.type = {
    additionalClasses += s" $clas"
    this
  }

  def label: String

  def renderTheadTrTh()(implicit
      fsc: FSContext,
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
  type C = Table6StandardColumn[R]

  def ColStr(title: String, render: R => String, renderFoot: Seq[R] => String = _ => "") = new Table6StandardColumn[R] {

    override def label: String = title

    override def renderTheadTrTh()(implicit
        fsc: FSContext,
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
    ): Elem = <th>{renderFoot(rowsWithIds.map(_._2))}</th>
  }

  def ColNs(title: String, render: FSContext => R => NodeSeq, renderFoot: FSContext => Seq[R] => NodeSeq = _ => _ => NodeSeq.Empty) = new Table6StandardColumn[R] {

    override def label: String = title

    override def renderTheadTrTh()(implicit
        fsc: FSContext,
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
    ): Elem = <th>{renderFoot(fsc)(rowsWithIds.map(_._2))}</th>
  }

  def ColNsFull(
      title: String,
      render: FSContext => (
          Seq[(String, R)],
          Seq[(String, C)],
          TableWrapperRerenderer,
          TableRerenderer,
          TableHeadRerenderer,
          TableBodyRerenderer,
          TableFootRerenderer,
          TrRerenderer,
          TdRerenderer,
          C,
          TableColIdx,
          TableColId,
          R,
          TableRowIdx,
          TableRowId
      ) => NodeSeq,
      renderFoot: FSContext => (
          Seq[(String, R)],
          Seq[(String, C)],
          TableWrapperRerenderer,
          TableRerenderer,
          TableHeadRerenderer,
          TableBodyRerenderer,
          TableFootRerenderer,
          TrRerenderer,
          ThRerenderer,
          C,
          TableColIdx,
          TableColId
      ) => NodeSeq = _ => (_, _, _, _, _, _, _, _, _, _, _, _) => NodeSeq.Empty
  ) = new Table6StandardColumn[R] {

    override def label: String = title

    override def renderTheadTrTh()(implicit
        fsc: FSContext,
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
    ): Elem = <td class={additionalClasses}>{
      render(fsc)(
        rowsWithIds,
        columnsWithIds,
        tableWrapperRenderer,
        tableRenderer,
        tableHeadRerenderer,
        tableBodyRerenderer,
        tableFootRerenderer,
        trRerenderer,
        tdRerenderer,
        col,
        colIdx,
        colId,
        row,
        rowIdx,
        rowId
      )
    }</td>

    override def renderTfootTrTh()(implicit
        fsc: FSContext,
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
    ): Elem = <th>{
      renderFoot(fsc)(
        rowsWithIds,
        columnsWithIds,
        tableWrapperRenderer,
        tableRenderer,
        tableHeadRerenderer,
        tableBodyRerenderer,
        tableFootRerenderer,
        trRerenderer,
        thRerenderer,
        col,
        colIdx,
        colId
      )
    }</th>
  }

  def ColNsFullTd(
      title: String,
      render: FSContext => (
          Seq[(String, R)],
          Seq[(String, C)],
          TableWrapperRerenderer,
          TableRerenderer,
          TableHeadRerenderer,
          TableBodyRerenderer,
          TableFootRerenderer,
          TrRerenderer,
          TdRerenderer,
          C,
          TableColIdx,
          TableColId,
          R,
          TableRowIdx,
          TableRowId
      ) => Elem,
      renderFoot: FSContext => (
          Seq[(String, R)],
          Seq[(String, C)],
          TableWrapperRerenderer,
          TableRerenderer,
          TableHeadRerenderer,
          TableBodyRerenderer,
          TableFootRerenderer,
          TrRerenderer,
          ThRerenderer,
          C,
          TableColIdx,
          TableColId
      ) => Elem = _ => (_, _, _, _, _, _, _, _, _, _, _, _) => <th></th>
  ) = new Table6StandardColumn[R] {

    override def label: String = title

    override def renderTheadTrTh()(implicit
        fsc: FSContext,
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
    ): Elem = render(fsc)(
      rowsWithIds,
      columnsWithIds,
      tableWrapperRenderer,
      tableRenderer,
      tableHeadRerenderer,
      tableBodyRerenderer,
      tableFootRerenderer,
      trRerenderer,
      tdRerenderer,
      col,
      colIdx,
      colId,
      row,
      rowIdx,
      rowId
    ).withClass(additionalClasses)

    override def renderTfootTrTh()(implicit
        fsc: FSContext,
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
    ): Elem = renderFoot(fsc)(
      rowsWithIds,
      columnsWithIds,
      tableWrapperRenderer,
      tableRenderer,
      tableHeadRerenderer,
      tableBodyRerenderer,
      tableFootRerenderer,
      trRerenderer,
      thRerenderer,
      col,
      colIdx,
      colId
    )
  }
}
