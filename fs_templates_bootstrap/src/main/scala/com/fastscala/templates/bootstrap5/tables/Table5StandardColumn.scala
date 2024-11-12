package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.FSContext
import com.fastscala.templates.bootstrap5.helpers.BSHelpers.RichElemBootstrapClasses
import com.fastscala.templates.bootstrap5.helpers.ClassEnrichableMutable

import scala.xml.{Elem, NodeSeq}

trait Table5StandardColumns extends Table5ColsRenderable with Table5ColsLabeled with Table5StdColsHelper {

  type C = Table5StandardColumn[R]

  override def renderTRTD()(
    implicit tableBodyRerenderer: TableBodyRerenderer,
    trRerenderer: TRRerenderer,
    tdRerenderer: TDRerenderer,
    value: R,
    rowIdx: TableRowIdx,
    columns: Seq[(String, Table5StandardColumn[R])],
    rows: Seq[(String, R)],
    colThId: String,
    col: Table5StandardColumn[R],
    tableColIdx: TableColIdx,
    fsc: FSContext
  ): Elem = col.renderTD()

  override def renderTableHeadTRTH()(
    implicit tableHeadRerenderer: TableHeadRerenderer,
    trRerenderer: TRRerenderer,
    thRerenderer: THRerenderer,
    columns: Seq[(String, Table5StandardColumn[R])],
    rows: Seq[(String, R)],
    colThId: String,
    col: Table5StandardColumn[R],
    tableColIdx: TableColIdx,
    fsc: FSContext
  ): Elem = col.renderTH()

  override def colLabel(col: C): String = col.label
}

trait Table5StandardColumn[R] extends ClassEnrichableMutable {

  var additionalClasses = ""

  override def addClass(clas: String): this.type = {
    additionalClasses += s" $clas"
    this
  }

  def label: String

  def renderTH()(
    implicit
    tableHeadRerenderer: TableHeadRerenderer,
    trRerenderer: TRRerenderer,
    thRerenderer: THRerenderer,
    colIdx: TableColIdx,
    pageRows: Seq[(String, R)],
    fsc: FSContext
  ): Elem

  def renderTD()(
    implicit
    tableBodyRerenderer: TableBodyRerenderer,
    trRerenderer: TRRerenderer,
    tdRerenderer: TDRerenderer,
    value: R,
    rowIdx: TableRowIdx,
    colIdx: TableColIdx,
    rows: Seq[(String, R)],
    fsc: FSContext
  ): Elem
}

trait Table5StdColsHelper {

  type R

  def ColStr(title: String, render: R => String) = new Table5StandardColumn[R] {

    override def label: String = title

    override def renderTH()(implicit tableHeadRerenderer: TableHeadRerenderer, trRerenderer: TRRerenderer, thRerenderer: THRerenderer, colIdx: TableColIdx, pageRows: Seq[(String, R)], fsc: FSContext): Elem =
      <th>{title}</th>

    override def renderTD()(implicit tableBodyRerenderer: TableBodyRerenderer, trRerenderer: TRRerenderer, tdRerenderer: TDRerenderer, value: R, rowIdx: TableRowIdx, colIdx: TableColIdx, rows: Seq[(String, R)], fsc: FSContext): Elem =
      <td class={additionalClasses}>{render(value)}</td>
  }

  def ColNs(title: String, render: FSContext => R => NodeSeq) = new Table5StandardColumn[R] {

    override def label: String = title

    override def renderTH()(implicit tableHeadRerenderer: TableHeadRerenderer, trRerenderer: TRRerenderer, thRerenderer: THRerenderer, colIdx: TableColIdx, pageRows: Seq[(String, R)], fsc: FSContext): Elem =
      <th>{title}</th>

    override def renderTD()(implicit tableBodyRerenderer: TableBodyRerenderer, trRerenderer: TRRerenderer, tdRerenderer: TDRerenderer, value: R, rowIdx: TableRowIdx, colIdx: TableColIdx, rows: Seq[(String, R)], fsc: FSContext): Elem =
      <td class={additionalClasses}>{render(fsc)(value)}</td>
  }

  def ColNsFull(
                 title: String,
                 render: FSContext => (TableBodyRerenderer, TRRerenderer, TDRerenderer, R, TableRowIdx, TableColIdx, Seq[(String, R)]) => NodeSeq
               ) = new Table5StandardColumn[R] {

    override def label: String = title

    override def renderTH()(implicit tableHeadRerenderer: TableHeadRerenderer, trRerenderer: TRRerenderer, thRerenderer: THRerenderer, colIdx: TableColIdx, pageRows: Seq[(String, R)], fsc: FSContext): Elem =
      <th>{title}</th>

    override def renderTD()(implicit tableBodyRerenderer: TableBodyRerenderer, trRerenderer: TRRerenderer, tdRerenderer: TDRerenderer, value: R, rowIdx: TableRowIdx, colIdx: TableColIdx, rows: Seq[(String, R)], fsc: FSContext): Elem =
      <td class={additionalClasses}>{render(fsc)(tableBodyRerenderer, trRerenderer, tdRerenderer, value, rowIdx, colIdx, rows)}</td>
  }

  def ColNsFullTd(
                   title: String,
                   render: FSContext => (TableBodyRerenderer, TRRerenderer, TDRerenderer, R, TableRowIdx, TableColIdx, Seq[(String, R)]) => Elem
                 ) = new Table5StandardColumn[R] {

    override def label: String = title

    override def renderTH()(implicit tableHeadRerenderer: TableHeadRerenderer, trRerenderer: TRRerenderer, thRerenderer: THRerenderer, colIdx: TableColIdx, pageRows: Seq[(String, R)], fsc: FSContext): Elem =
      <th>{title}</th>

    override def renderTD()(implicit tableBodyRerenderer: TableBodyRerenderer, trRerenderer: TRRerenderer, tdRerenderer: TDRerenderer, value: R, rowIdx: TableRowIdx, colIdx: TableColIdx, rows: Seq[(String, R)], fsc: FSContext): Elem =
      render(fsc)(tableBodyRerenderer, trRerenderer, tdRerenderer, value, rowIdx, colIdx, rows).withClass(additionalClasses)
  }
}