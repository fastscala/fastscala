package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.helpers.BSHelpers.RichElemBasicOps
import com.fastscala.templates.bootstrap5.helpers.ClassEnrichable
import com.fastscala.utils.IdGen
import com.fastscala.xml.scala_xml.JS
import com.fastscala.xml.scala_xml.ScalaXmlNodeSeqUtils.{MkNSFromElems, MkNSFromNodeSeq}

import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

abstract class Table5Base() extends Table5ColsRenderable with ClassEnrichable {

  type R
  type C

  lazy val aroundId: AroundId = new AroundId(IdGen.id("around"))
  lazy val tableId: TableId = new TableId(IdGen.id("table"))

  var additionalTableClasses: String = ""
  var additionalTableHeadClasses: String = ""
  var additionalTableHeadTRClasses: String = ""
  var additionalTableHeadTRTHClasses: String = ""
  var additionalTableBodyClasses: String = ""
  var additionalTableBodyTRClasses: String = ""
  var additionalTableBodyTRTDClasses: String = ""

  override def setClass(clas: String): this.type = {
    additionalTableClasses += s" $clas"
    this
  }

  def tableClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = additionalTableClasses

  def tableStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""


  def tableHeadClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = additionalTableHeadClasses

  def tableHeadStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def onAllTableHeadClasses(f: ClassEnrichable => Unit): this.type = {
    f(new ClassEnrichable {
      override def setClass(clas: String): this.type = {
        additionalTableHeadClasses += s" $clas"
        this
      }
    })
    this
  }

  def tableHeadTRClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = additionalTableHeadTRClasses

  def tableHeadTRStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def onAllTableHeadTRClasses(f: ClassEnrichable => Unit): this.type = {
    f(new ClassEnrichable {
      override def setClass(clas: String): this.type = {
        additionalTableHeadTRClasses += s" $clas"
        this
      }
    })
    this
  }

  def tableHeadTRTHClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = additionalTableHeadTRTHClasses

  def tableHeadTRTHStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def onAllTableHeadTRTHClasses(f: ClassEnrichable => Unit): this.type = {
    f(new ClassEnrichable {
      override def setClass(clas: String): this.type = {
        additionalTableHeadTRTHClasses += s" $clas"
        this
      }
    })
    this
  }

  def tableBodyClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = additionalTableBodyClasses

  def tableBodyStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def onAllTableBodyClasses(f: ClassEnrichable => Unit): this.type = {
    f(new ClassEnrichable {
      override def setClass(clas: String): this.type = {
        additionalTableBodyClasses += s" $clas"
        this
      }
    })
    this
  }

  def tableBodyTRClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = additionalTableBodyTRClasses

  def tableBodyTRStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def onAllTableBodyTRClasses(f: ClassEnrichable => Unit): this.type = {
    f(new ClassEnrichable {
      override def setClass(clas: String): this.type = {
        additionalTableBodyTRClasses += s" $clas"
        this
      }
    })
    this
  }

  def tableBodyTRTDClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = additionalTableBodyTRTDClasses

  def tableBodyTRTDStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def onAllTableBodyTRTDClasses(f: ClassEnrichable => Unit): this.type = {
    f(new ClassEnrichable {
      override def setClass(clas: String): this.type = {
        additionalTableBodyTRTDClasses += s" $clas"
        this
      }
    })
    this
  }

  def columns(): Seq[C]

  def rowsHints(): Seq[RowsHint] = Nil

  def rows(hints: Seq[RowsHint] = rowsHints()): Seq[R]

  def idForRow(row: R, rowIdx: Int): String = "rowId" + rowIdx

  def idForColumn(col: C): String = "colId" + UUID.randomUUID()

  def render()(implicit fsc: FSContext): Elem = aroundRenderer.rerenderer.render()

  // ### Around level: ###
  protected implicit lazy val aroundRenderer: AroundRerenderer = AroundRerenderer(JS.rerenderable(implicit rerenderer => implicit fsc => {
    renderTableAround()
  }, idOpt = Some(aroundId.id), debugLabel = Some("around_table")))

  def rerenderTableAround()(implicit fsc: FSContext): Js = aroundRenderer.rerenderer.rerender()

  def aroundClasses()(implicit fsc: FSContext): String = ""

  def aroundStyle()(implicit fsc: FSContext): String = ""

  def renderTableAround()(implicit fsc: FSContext): Elem = <div class={aroundClasses()} style={aroundStyle()}>{renderAroundContents()}</div>

  def renderAroundContents()(implicit fsc: FSContext): NodeSeq = tableRenderer.rerenderer.render()

  // ### Table level: ###
  protected implicit lazy val tableRenderer: TableRerenderer = TableRerenderer(JS.rerenderable(implicit rerenderer => implicit fsc => {
    renderTable()
  }, idOpt = Some(tableId.id), debugLabel = Some("table")))

  def transformTableElem(elem: Elem)(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): Elem = elem.withClass(tableClasses()).withStyle(tableStyle())

  def transformTableHeadElem(elem: Elem)(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): Elem = elem.withClass(tableHeadClasses()).withStyle(tableHeadStyle())

  def transformTableBodyElem(elem: Elem)(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): Elem = elem.withClass(tableBodyClasses()).withStyle(tableBodyStyle())

  def transformTableHeadTRElem(elem: Elem)(
    implicit
    tableHeadRerenderer: TableHeadRerenderer
    , rowsWithIds: Seq[(String, R)]
    , columns: Seq[(String, C)]
    , fsc: FSContext
  ): Elem = elem.withClass(tableHeadTRClasses()).withStyle(tableHeadTRStyle())

  def transformTRElem(elem: Elem)(
    implicit
    tableBodyRerenderer: TableBodyRerenderer,
    trRerenderer: TRRerenderer,
    value: R,
    rowIdx: TableRowIdx,
    columns: Seq[(String, C)],
    rows: Seq[(String, R)],
    fsc: FSContext
  ): Elem = elem.withClass(tableBodyTRClasses()).withStyle(tableBodyTRStyle())

  def transformTableHeadTRTHElem(elem: Elem)(
    implicit
    tableHeadRerenderer: TableHeadRerenderer,
    trRerenderer: TRRerenderer,
    columns: Seq[(String, C)],
    rows: Seq[(String, R)],
    fsc: FSContext
  ): Elem = elem.withClass(tableHeadTRTHClasses()).withStyle(tableHeadTRTHStyle())

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
  ): Elem = elem.withClass(tableBodyTRTDClasses()).withStyle(tableBodyTRTDStyle())

  def rerenderTable()(implicit fsc: FSContext): Js = tableRenderer.rerenderer.rerender()

  def renderTable()(implicit fsc: FSContext): Elem = {

    implicit val rowsWithIds: Seq[(String, R)] = rows().zipWithIndex.map({
      case (row, rowIdx) => (idForRow(row, rowIdx), row)
    })
    implicit val columnsWithIds: Seq[(String, C)] = columns().map(col => (idForColumn(col), col))

    val tableContents = renderTableContents()

    <table id={tableId.id}>{tableContents}</table>.pipe(transformTableElem)
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
  def renderTableBody()(implicit columnsWithIds: Seq[(String, C)], rowsWithIds: Seq[(String, R)], fsc: FSContext): Elem = {
    JS.rerenderable(implicit rerenderer => implicit fsc => {
      implicit val tableBodyRerenderer: TableBodyRerenderer = TableBodyRerenderer(rerenderer)
      val contents = renderTableBodyContents()
      <tbody id={rerenderer.aroundId}>{contents}</tbody>
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
        val rerenderable = JS.rerenderable(
          implicit rerenderer =>
            implicit fsc => {
              implicit val trRerenderer: TRRerenderer = TRRerenderer(rerenderer)
              renderTR().pipe(transformTRElem)
            },
          idOpt = Some(rowId),
          debugLabel = Some(s"table_row_${rowIdx}")
        )
        renderTRPrepend() ++
          rerenderable.render() ++
          renderTRAppend()
    }).mkNS
  }

  def renderTRPrepend()(
    implicit
    tableBodyRerenderer: TableBodyRerenderer,
    value: R,
    rowIdx: TableRowIdx,
    columns: Seq[(String, C)],
    rows: Seq[(String, R)],
    fsc: FSContext
  ): NodeSeq = NodeSeq.Empty

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

    val trContents = renderTRContents()
    <tr>{trContents}</tr>
  }

  def renderTRAppend()(
    implicit
    tableBodyRerenderer: TableBodyRerenderer,
    value: R,
    rowIdx: TableRowIdx,
    columns: Seq[(String, C)],
    rows: Seq[(String, R)],
    fsc: FSContext
  ): NodeSeq = NodeSeq.Empty

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
        JS.rerenderable(
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
  def renderTableHead()(implicit columnsWithIds: Seq[(String, C)], rowsWithIds: Seq[(String, R)], fsc: FSContext): Elem = {
    JS.rerenderable(implicit rerenderer => implicit fsc => {
      implicit val tableHeadRerenderer: TableHeadRerenderer = TableHeadRerenderer(rerenderer)
      val contents = renderTableHeadContents()
      <thead id={rerenderer.aroundId}>{contents}</thead>
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
    val rerenderer = JS.rerenderable(implicit rerenderer =>
      implicit fsc => {
        implicit val trRerenderer: TRRerenderer = TRRerenderer(rerenderer)
        renderTableHeadTR().pipe(transformTableHeadTRElem)
      },
      debugLabel = Some(s"table_head_row")
    )
    renderTableHeadTRPrepend() ++
      rerenderer.render() ++
      renderTableHeadTRAppend()
  }

  def renderTableHeadTRPrepend()(
    implicit
    tableHeadRerenderer: TableHeadRerenderer,
    columns: Seq[(String, C)],
    rows: Seq[(String, R)],
    fsc: FSContext
  ): NodeSeq = NodeSeq.Empty

  def renderTableHeadTR()(
    implicit
    tableHeadRerenderer: TableHeadRerenderer,
    trRerenderer: TRRerenderer,
    columns: Seq[(String, C)],
    rows: Seq[(String, R)],
    fsc: FSContext
  ): Elem = {

    val trContents = renderTableHeadTRContents()
    <tr>{trContents}</tr>
  }

  def renderTableHeadTRAppend()(
    implicit
    tableHeadRerenderer: TableHeadRerenderer,
    columns: Seq[(String, C)],
    rows: Seq[(String, R)],
    fsc: FSContext
  ): NodeSeq = NodeSeq.Empty

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
        JS.rerenderable(
          implicit rerenderer =>
            implicit fsc => {
              implicit val thRerenderer: THRerenderer = THRerenderer(rerenderer)
              implicit val _colThId: String = colThId
              implicit val _col: C = col
              implicit val _tableColIdx: TableColIdx = new TableColIdx(colIdx)
              renderTableHeadTRTH().pipe(transformTableHeadTRTHElem)
            },
          debugLabel = Some(s"table_head_row_col_${colIdx}")
        ).render()
    }).mkNS
  }
}
