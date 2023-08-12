package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.FSContext
import com.fastscala.js.{Js, Rerenderer}
import com.fastscala.utils.IdGen
import com.fastscala.utils.NodeSeqUtils.MkNSFromNodeSeq

import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

abstract class Table5Base() extends Table5ColsRenderable {

  type R
  type C

  lazy val aroundId: AroundId = new AroundId(IdGen.id("around"))
  lazy val tableId: TableId = new TableId(IdGen.id("table"))

  def columns(): Seq[C]

  def rowsHints(): Seq[RowsHint] = Nil

  def rows(hints: Seq[RowsHint] = rowsHints()): Seq[R]

  def idForRow(row: R, rowIdx: Int): String = "rowId" + rowIdx

  def idForColumn(col: C): String = "colId" + UUID.randomUUID()

  def render()(implicit fsc: FSContext): Elem = aroundRenderer.rerenderer.render()

  // ### Around level: ###
  protected implicit lazy val aroundRenderer = AroundRerenderer(Js.rerenderable(implicit rerenderer => implicit fsc => {
    renderTableAround()
  }, idOpt = Some(aroundId.id), debugLabel = Some("around_table")))

  def rerenderTableAround()(implicit fsc: FSContext): Js = aroundRenderer.rerenderer.rerender()

  def aroundClasses()(implicit fsc: FSContext): String = ""

  def aroundStyle()(implicit fsc: FSContext): String = ""

  def renderTableAround()(implicit fsc: FSContext): Elem = <div class={aroundClasses()} style={aroundStyle()}>{renderAroundContents()}</div>

  def renderAroundContents()(implicit fsc: FSContext): NodeSeq = tableRenderer.rerenderer.render()

  // ### Table level: ###
  protected implicit lazy val tableRenderer = TableRerenderer(Js.rerenderable(implicit rerenderer => implicit fsc => {
    renderTable()
  }, idOpt = Some(tableId.id), debugLabel = Some("table")))

  def transformTableElem(elem: Elem)(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): Elem = elem

  def transformTableHeadElem(elem: Elem)(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): Elem = elem

  def transformTableBodyElem(elem: Elem)(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): Elem = elem

  def transformTableHeadTRElem(elem: Elem)(
    implicit
    tableHeadRerenderer: TableHeadRerenderer
    , rowsWithIds: Seq[(String, R)]
    , columns: Seq[(String, C)]
    , fsc: FSContext
  ): Elem = elem

  def transformTRElem(elem: Elem)(
    implicit
    tableBodyRerenderer: TableBodyRerenderer,
    trRerenderer: TRRerenderer,
    value: R,
    rowIdx: TableRowIdx,
    columns: Seq[(String, C)],
    rows: Seq[(String, R)],
    fsc: FSContext
  ): Elem = elem

  def transformTableHeadTRTDElem(elem: Elem)(
    implicit
    tableHeadRerenderer: TableHeadRerenderer,
    trRerenderer: TRRerenderer,
    columns: Seq[(String, C)],
    rows: Seq[(String, R)],
    fsc: FSContext
  ): Elem = elem

  def transformTRTDElem(elem: Elem)(
    implicit
    tableBodyRerenderer: TableBodyRerenderer,
    trRerenderer: TRRerenderer,
    col: C,
    value: R,
    rowIdx: TableRowIdx,
    columns: Seq[(String, C)],
    rows: Seq[(String, R)],
    fsc: FSContext
  ): Elem = elem

  def tableClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def tableStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def rerenderTable()(implicit fsc: FSContext): Js = tableRenderer.rerenderer.rerender()

  def renderTable()(implicit fsc: FSContext): Elem = {

    implicit val rowsWithIds: Seq[(String, R)] = rows().zipWithIndex.map({
      case (row, rowIdx) => (idForRow(row, rowIdx), row)
    })
    implicit val columnsWithIds: Seq[(String, C)] = columns().map(col => (idForColumn(col), col))

    val classes = tableClasses()
    val style = tableStyle()

    val tableContents = renderTableContents()

    <table class={classes} style={style} id={tableId.id}>{tableContents}</table>.pipe(transformTableElem)
  }

  def renderTableContents()(
    implicit
    columns: Seq[(String, C)]
    , rows: Seq[(String, R)]
    , fsc: FSContext
  ): NodeSeq = {
    renderTableHead().pipe(transformTableHeadElem) ++
      renderTableBody().pipe(transformTableBodyElem)
  }

  // ### Table body: ###
  def tableBodyClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def tableBodyStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def renderTableBody()(implicit columnsWithIds: Seq[(String, C)], rowsWithIds: Seq[(String, R)], fsc: FSContext): Elem = {
    val classes = tableBodyClasses()
    val style = tableBodyStyle()

    Js.rerenderable(implicit rerenderer => implicit fsc => {
      implicit val tableBodyRerenderer: TableBodyRerenderer = TableBodyRerenderer(rerenderer)
      val contents = renderTableBodyContents()
      <tbody class={classes} style={style} id={rerenderer.aroundId}>{contents}</tbody>
    }, debugLabel = Some("table_body")).render()
  }

  def renderTableBodyContents()(
    implicit
    tableBodyRerenderer: TableBodyRerenderer
    , rowsWithIds: Seq[(String, R)]
    , columns: Seq[(String, C)]
    , fsc: FSContext
  ): NodeSeq = {
    rowsWithIds.zipWithIndex.map({
      case ((rowId, value), rowIdx) =>
        implicit val _rowId = rowId
        implicit val _value = value
        implicit val _rowIdx = new TableRowIdx(rowIdx)
        Js.rerenderable(
          implicit rerenderer =>
            implicit fsc => {
              implicit val trRerenderer: TRRerenderer = TRRerenderer(rerenderer)
              renderTR().pipe(transformTRElem)
            },
          idOpt = Some(rowId),
          debugLabel = Some(s"table_row_${rowIdx}")
        ).render()
    }).mkNS
  }

  def trClasses()(implicit value: R, rowIdx: TableRowIdx, columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def trStyle()(implicit value: R, rowIdx: TableRowIdx, columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def renderTR()(
    implicit
    tableBodyRerenderer: TableBodyRerenderer,
    trRerenderer: TRRerenderer,
    value: R,
    rowIdx: TableRowIdx,
    columns: Seq[(String, C)],
    rows: Seq[(String, R)],
    fsc: FSContext
  ): Elem = {

    val classes = trClasses()
    val style = trStyle()

    val trContents = renderTRContents()
    <tr class={classes} style={style}>{trContents}</tr>
  }

  def renderTRContents()(
    implicit
    tableBodyRerenderer: TableBodyRerenderer,
    trRerenderer: TRRerenderer,
    value: R,
    rowIdx: TableRowIdx,
    columns: Seq[(String, C)],
    rows: Seq[(String, R)],
    fsc: FSContext
  ): NodeSeq = {

    columns.zipWithIndex.map({
      case ((colThId, col), colIdx) =>
        Js.rerenderable(
          implicit rerenderer =>
            implicit fsc => {
              implicit val tdRerenderer: TDRerenderer = TDRerenderer(rerenderer)
              implicit val _colThId: String = colThId
              implicit val _col: C = col
              implicit val _tableColIdx: TableColIdx = new TableColIdx(colIdx)
              renderTRTD().pipe(transformTRTDElem)
            },
          debugLabel = Some(s"table_row_${rowIdx.idx}_col_${colIdx}")
        ).render()
    }).mkNS
  }

  // ### Table head: ###
  def tableHeadClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def tableHeadStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def renderTableHead()(implicit columnsWithIds: Seq[(String, C)], rowsWithIds: Seq[(String, R)], fsc: FSContext): Elem = {
    val classes = tableHeadClasses()
    val style = tableHeadStyle()

    Js.rerenderable(implicit rerenderer => implicit fsc => {
      implicit val tableHeadRerenderer: TableHeadRerenderer = TableHeadRerenderer(rerenderer)
      val contents = renderTableHeadContents()
      <thead class={classes} style={style} id={rerenderer.aroundId}>{contents}</thead>
    },
      debugLabel = Some(s"table_head")
    ).render()
  }

  def renderTableHeadContents()(
    implicit
    tableHeadRerenderer: TableHeadRerenderer
    , rowsWithIds: Seq[(String, R)]
    , columns: Seq[(String, C)]
    , fsc: FSContext
  ): NodeSeq = {
    Js.rerenderable(implicit rerenderer =>
      implicit fsc => {
        implicit val trRerenderer: TRRerenderer = TRRerenderer(rerenderer)
        renderTableHeadTR().pipe(transformTableHeadTRElem)
      },
      debugLabel = Some(s"table_head_row")
    ).render()
  }

  def theadTRClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def theadTRStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def renderTableHeadTR()(
    implicit
    tableHeadRerenderer: TableHeadRerenderer,
    trRerenderer: TRRerenderer,
    columns: Seq[(String, C)],
    rows: Seq[(String, R)],
    fsc: FSContext
  ): Elem = {

    val classes = theadTRClasses()
    val style = theadTRStyle()

    val trContents = renderTableHeadTRContents()
    <tr class={classes} style={style}>{trContents}</tr>
  }

  def renderTableHeadTRContents()(
    implicit
    tableHeadRerenderer: TableHeadRerenderer,
    trRerenderer: TRRerenderer,
    columns: Seq[(String, C)],
    rows: Seq[(String, R)],
    fsc: FSContext
  ): NodeSeq = {

    columns.zipWithIndex.map({
      case ((colThId, col), colIdx) =>
        Js.rerenderable(
          implicit rerenderer =>
            implicit fsc => {
              implicit val thRerenderer: THRerenderer = THRerenderer(rerenderer)
              implicit val _colThId: String = colThId
              implicit val _col: C = col
              implicit val _tableColIdx: TableColIdx = new TableColIdx(colIdx)
              renderTableHeadTRTH().pipe(transformTableHeadTRTDElem)
            },
          debugLabel = Some(s"table_head_row_col_${colIdx}")
        ).render()
    }).mkNS
  }
}
