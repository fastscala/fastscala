package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.xml.scala_xml.FSScalaXmlSupport.RichElem

trait Table5StandardColumns[E <: FSXmlEnv] extends Table5ColsRenderable[E] with Table5ColsLabeled with Table5StdColsHelper {

  type C = Table5StandardColumn[E, R]

  override def renderTRTD()(
    implicit tableBodyRerenderer: TableBodyRerenderer[E],
    trRerenderer: TRRerenderer[E],
    tdRerenderer: TDRerenderer[E],
    value: R,
    rowIdx: TableRowIdx,
    columns: Seq[(String, Table5StandardColumn[E, R])],
    rows: Seq[(String, R)],
    colThId: String,
    col: Table5StandardColumn[E, R],
    tableColIdx: TableColIdx,
    fsc: FSContext
  ): E#Elem = col.renderTD()

  override def renderTableHeadTRTH()(
    implicit tableHeadRerenderer: TableHeadRerenderer[E],
    trRerenderer: TRRerenderer[E],
    thRerenderer: THRerenderer[E],
    columns: Seq[(String, Table5StandardColumn[E, R])],
    rows: Seq[(String, R)],
    colThId: String,
    col: Table5StandardColumn[E, R],
    tableColIdx: TableColIdx,
    fsc: FSContext
  ): E#Elem = col.renderTH()

  override def colLabel(col: C): String = col.label
}

trait Table5StandardColumn[E <: FSXmlEnv, R] {

  def label: String

  def renderTH()(
    implicit
    tableHeadRerenderer: TableHeadRerenderer[E],
    trRerenderer: TRRerenderer[E],
    thRerenderer: THRerenderer[E],
    colIdx: TableColIdx,
    pageRows: Seq[(String, R)],
    fsc: FSContext
  ): E#Elem

  def renderTD()(
    implicit
    tableBodyRerenderer: TableBodyRerenderer[E],
    trRerenderer: TRRerenderer[E],
    tdRerenderer: TDRerenderer[E],
    value: R,
    rowIdx: TableRowIdx,
    colIdx: TableColIdx,
    rows: Seq[(String, R)],
    fsc: FSContext
  ): E#Elem
}

trait Table5StdColsHelper {

  type R

  def ColStr[E <: FSXmlEnv : FSXmlSupport](title: String, render: R => String) = new Table5StandardColumn[E, R] {

    override def label: String = title

    override def renderTH()(implicit tableHeadRerenderer: TableHeadRerenderer[E], trRerenderer: TRRerenderer[E], thRerenderer: THRerenderer[E], colIdx: TableColIdx, pageRows: Seq[(String, R)], fsc: FSContext): E#Elem =
      <th>{title}</th>.asFSXml()

    override def renderTD()(implicit tableBodyRerenderer: TableBodyRerenderer[E], trRerenderer: TRRerenderer[E], tdRerenderer: TDRerenderer[E], value: R, rowIdx: TableRowIdx, colIdx: TableColIdx, rows: Seq[(String, R)], fsc: FSContext): E#Elem =
      <td>{render(value)}</td>.asFSXml()
  }

  def ColNs[E <: FSXmlEnv : FSXmlSupport](title: String, render: FSContext => R => E#NodeSeq) = new Table5StandardColumn[E, R] {

    override def label: String = title

    override def renderTH()(implicit tableHeadRerenderer: TableHeadRerenderer[E], trRerenderer: TRRerenderer[E], thRerenderer: THRerenderer[E], colIdx: TableColIdx, pageRows: Seq[(String, R)], fsc: FSContext): E#Elem =
      <th>{title}</th>.asFSXml()

    override def renderTD()(implicit tableBodyRerenderer: TableBodyRerenderer[E], trRerenderer: TRRerenderer[E], tdRerenderer: TDRerenderer[E], value: R, rowIdx: TableRowIdx, colIdx: TableColIdx, rows: Seq[(String, R)], fsc: FSContext): E#Elem =
      <td>{render(fsc)(value)}</td>.asFSXml()
  }

  def ColNsFull[E <: FSXmlEnv : FSXmlSupport](
                                               title: String,
                                               render: FSContext => (TableBodyRerenderer[E], TRRerenderer[E], TDRerenderer[E], R, TableRowIdx, TableColIdx, Seq[(String, R)]) => E#NodeSeq
                                             ) = new Table5StandardColumn[E, R] {

    override def label: String = title

    override def renderTH()(implicit tableHeadRerenderer: TableHeadRerenderer[E], trRerenderer: TRRerenderer[E], thRerenderer: THRerenderer[E], colIdx: TableColIdx, pageRows: Seq[(String, R)], fsc: FSContext): E#Elem =
      <th>{title}</th>.asFSXml()

    override def renderTD()(implicit tableBodyRerenderer: TableBodyRerenderer[E], trRerenderer: TRRerenderer[E], tdRerenderer: TDRerenderer[E], value: R, rowIdx: TableRowIdx, colIdx: TableColIdx, rows: Seq[(String, R)], fsc: FSContext): E#Elem =
      <td>{render(fsc)(tableBodyRerenderer, trRerenderer, tdRerenderer, value, rowIdx, colIdx, rows)}</td>.asFSXml()
  }

  def ColNsFullTd[E <: FSXmlEnv : FSXmlSupport](
                                                 title: String,
                                                 render: FSContext => (TableBodyRerenderer[E], TRRerenderer[E], TDRerenderer[E], R, TableRowIdx, TableColIdx, Seq[(String, R)]) => E#Elem
                                               ) = new Table5StandardColumn[E, R] {

    override def label: String = title

    override def renderTH()(implicit tableHeadRerenderer: TableHeadRerenderer[E], trRerenderer: TRRerenderer[E], thRerenderer: THRerenderer[E], colIdx: TableColIdx, pageRows: Seq[(String, R)], fsc: FSContext): E#Elem =
      <th>{title}</th>.asFSXml()

    override def renderTD()(implicit tableBodyRerenderer: TableBodyRerenderer[E], trRerenderer: TRRerenderer[E], tdRerenderer: TDRerenderer[E], value: R, rowIdx: TableRowIdx, colIdx: TableColIdx, rows: Seq[(String, R)], fsc: FSContext): E#Elem =
      render(fsc)(tableBodyRerenderer, trRerenderer, tdRerenderer, value, rowIdx, colIdx, rows)
  }
}