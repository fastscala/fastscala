package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.bootstrap5.helpers.ClassEnrichableMutable
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.{MkNSFromElems, MkNSFromNodeSeq}
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.rerenderers.Rerenderer
import com.fastscala.utils.IdGen

import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

abstract class Table6Base extends Table6ColsRenderable with Mutable {

  type R
  type C

  lazy val tableWrapperId: String = IdGen.id("table_wrapper")
  lazy val tableId: String = IdGen.id("table")
  lazy val theadId: String = IdGen.id("thead")
  lazy val theadTrId: String = IdGen.id("thead_tr")
  lazy val tbodyId: String = IdGen.id("tbody")
  lazy val tfootId: String = IdGen.id("tfoot")
  lazy val tfootTrId: String = IdGen.id("tfoot_tr")

  var onTableWrapperTransforms: Elem => Elem = identity[Elem]
  var onTableTransforms: Elem => Elem = identity[Elem]
  var onTableHeadTransforms: Elem => Elem = identity[Elem]
  var onTableHeadTrTransforms: Elem => Elem = identity[Elem]
  var onTableHeadTrThTransforms: Elem => Elem = identity[Elem]
  var onTableBodyTransforms: Elem => Elem = identity[Elem]
  var onTableBodyTrTransforms: Elem => Elem = identity[Elem]
  var onTableBodyTrTdTransforms: Elem => Elem = identity[Elem]
  var onTableFootTransforms: Elem => Elem = identity[Elem]
  var onTableFootTrTransforms: Elem => Elem = identity[Elem]
  var onTableFootTrThTransforms: Elem => Elem = identity[Elem]

  // Table wrapper level:
  def tableWrapperClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def tableWrapperStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def onTableWrapper(f: Elem => Elem): this.type = mutate {
    onTableWrapperTransforms = onTableWrapperTransforms.pipe(onTableWrapperTransforms => elem => f(onTableWrapperTransforms(elem)))
  }

  // Table level:
  def tableClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def tableStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def onTable(f: Elem => Elem): this.type = mutate {
    onTableTransforms = onTableTransforms.pipe(onTableTransforms => elem => f(onTableTransforms(elem)))
  }

  // Table head level:
  def tableHeadClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def tableHeadStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def onTableHead(f: Elem => Elem): this.type = mutate {
    onTableHeadTransforms = onTableHeadTransforms.pipe(onTableHeadTransforms => elem => f(onTableHeadTransforms(elem)))
  }

  // Table head tr level:
  def tableHeadTrClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def tableHeadTrStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def onTableHeadTr(f: Elem => Elem): this.type = mutate {
    onTableHeadTrTransforms = onTableHeadTrTransforms.pipe(onTableHeadTRTransforms => elem => f(onTableHeadTRTransforms(elem)))
  }

  // Table head tr th level:
  def tableHeadTrThClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def tableHeadTrThStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def onAllTableHeadTrTh(f: Elem => Elem): this.type = mutate {
    onTableHeadTrThTransforms = onTableHeadTrThTransforms.pipe(onTableHeadTRTHTransforms => elem => f(onTableHeadTRTHTransforms(elem)))
  }

  // Table body level:
  def tableBodyClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def tableBodyStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def onTableBody(f: Elem => Elem): this.type = mutate {
    onTableBodyTransforms = onTableBodyTransforms.pipe(onTableBodyTransforms => elem => f(onTableBodyTransforms(elem)))
  }

  // Table body tr level:
  def tableBodyTRClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def tableBodyTRStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def onAllTableBodyTR(f: Elem => Elem): this.type = mutate {
    onTableBodyTrTransforms = onTableBodyTrTransforms.pipe(onTableBodyTRTransforms => elem => f(onTableBodyTRTransforms(elem)))
  }

  // Table body tr td level:
  def tableBodyTRTDClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def tableBodyTRTDStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def onAllTableBodyTRTD(f: Elem => Elem): this.type = mutate {
    onTableBodyTrTdTransforms = onTableBodyTrTdTransforms.pipe(onTableBodyTRTDTransforms => elem => f(onTableBodyTRTDTransforms(elem)))
  }

  // Table foot level:
  def tableFootClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def tableFootStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def onAllTableFoot(f: Elem => Elem): this.type = mutate {
    onTableFootTransforms = onTableFootTransforms.pipe(onTableFootTransforms => elem => f(onTableFootTransforms(elem)))
  }

  // Table foot tr level:
  def tableFootTRClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def tableFootTRStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def onAllTableFootTR(f: Elem => Elem): this.type = mutate {
    onTableFootTrTransforms = onTableFootTrTransforms.pipe(onTableFootTRTransforms => elem => f(onTableFootTRTransforms(elem)))
  }

  // Table foot tr td level:
  def tableFootTrThClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def tableFootTrThStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def onAllTableFootTrTh(f: Elem => Elem): this.type = mutate {
    onTableFootTrThTransforms = onTableFootTrThTransforms.pipe(onTableFootTrThTransforms => elem => f(onTableFootTrThTransforms(elem)))
  }

  // Transform methods:

  def transformTableWrapperElem(elem: Elem)(implicit fsc: FSContext, columns: Seq[(String, C)], rows: Seq[(String, R)]): Elem =
    onTableWrapperTransforms(elem.withClass(tableWrapperClasses()).withStyle(tableWrapperStyle()))

  def transformTableElem(elem: Elem)(implicit fsc: FSContext, columns: Seq[(String, C)], rows: Seq[(String, R)]): Elem = onTableTransforms(elem.withClass(tableClasses()).withStyle(tableStyle()))

  def transformTableHeadElem(elem: Elem)(implicit fsc: FSContext, columns: Seq[(String, C)], rows: Seq[(String, R)]): Elem =
    onTableHeadTransforms(elem.withClass(tableHeadClasses()).withStyle(tableHeadStyle()))

  def transformTableBodyElem(elem: Elem)(implicit fsc: FSContext, columns: Seq[(String, C)], rows: Seq[(String, R)]): Elem =
    onTableBodyTransforms(elem.withClass(tableBodyClasses()).withStyle(tableBodyStyle()))

  def transformTableFootElem(elem: Elem)(implicit fsc: FSContext, columns: Seq[(String, C)], rows: Seq[(String, R)]): Elem =
    onTableFootTransforms(elem.withClass(tableFootClasses()).withStyle(tableFootStyle()))

  def transformTableHeadTrElem(elem: Elem)(implicit fsc: FSContext, columns: Seq[(String, C)], rows: Seq[(String, R)], tableHeadRerenderer: TableHeadRerenderer): Elem =
    onTableHeadTrTransforms(elem.withClass(tableHeadTrClasses()).withStyle(tableHeadTrStyle()))

  def transformTableHeadTrThElem(elem: Elem)(implicit fsc: FSContext, columns: Seq[(String, C)], rows: Seq[(String, R)], tableHeadRerenderer: TableHeadRerenderer, trRerenderer: TrRerenderer): Elem =
    onTableHeadTrThTransforms(
      elem.withClass(tableHeadTrThClasses()).withStyle(tableHeadTrThStyle())
    )

  def transformTableBodyTrElem(elem: Elem)(implicit
      fsc: FSContext,
      columns: Seq[(String, C)],
      rows: Seq[(String, R)],
      tableBodyRerenderer: TableBodyRerenderer,
      trRerenderer: TrRerenderer,
      row: R,
      rowIdx: TableRowIdx,
      rowId: TableRowId
  ): Elem =
    onTableBodyTrTransforms(elem.withClass(tableBodyTRClasses()).withStyle(tableBodyTRStyle()))

  def transformTableBodyTrTdElem(
      elem: Elem
  )(implicit
      fsc: FSContext,
      columns: Seq[(String, C)],
      rows: Seq[(String, R)],
      tableBodyRerenderer: TableBodyRerenderer,
      trRerenderer: TrRerenderer,
      col: C,
      colIdx: TableColIdx,
      colId: TableColId,
      row: R,
      rowIdx: TableRowIdx,
      rowId: TableRowId
  ): Elem =
    onTableBodyTrTdTransforms(elem.withClass(tableBodyTRTDClasses()).withStyle(tableBodyTRTDStyle()))

  def transformTableFootTrElem(elem: Elem)(implicit fsc: FSContext, columns: Seq[(String, C)], rows: Seq[(String, R)], tableFootRerenderer: TableFootRerenderer, trRerenderer: TrRerenderer): Elem =
    onTableFootTrTransforms(elem.withClass(tableFootTRClasses()).withStyle(tableBodyTRStyle()))

  def transformTableFootTrThElem(elem: Elem)(implicit
      fsc: FSContext,
      columns: Seq[(String, C)],
      rows: Seq[(String, R)],
      tableFootRerenderer: TableFootRerenderer,
      trRerenderer: TrRerenderer,
      col: C,
      colIdx: TableColIdx,
      colId: TableColId
  ): Elem =
    onTableFootTrThTransforms(elem.withClass(tableFootTrThClasses()).withStyle(tableFootTrThStyle()))

  def columns(): Seq[C]

  def rowsHints(): Seq[RowsHint] = Nil

  /** Generates the rows and a context relevant for this rendering.
    */
  def rows(hints: Seq[RowsHint] = rowsHints()): Seq[R]

  def idForRow(row: R, rowIdx: Int): String = "rowId" + rowIdx

  def idForColumn(col: C, colIdx: Int): String = "colId" + UUID.randomUUID()

  // Rerenderers:
  protected implicit lazy val rerenderer: TableWrapperRerenderer = TableWrapperRerenderer(
    JS.rerenderable(
      implicit rerenderer =>
        implicit fsc => {

          implicit val rowsWithIds: Seq[(String, R)] = rows().zipWithIndex.map({ case (row, rowIdx) =>
            (idForRow(row, rowIdx), row)
          })
          implicit val columnsWithIds: Seq[(String, C)] = columns().zipWithIndex.map({ case (col, colIdx) =>
            (idForColumn(col, colIdx), col)
          })

          renderTableWrapper()
        },
      idOpt = Some(tableWrapperId),
      debugLabel = Some("table_wrapper")
    )
  )

  def render()(implicit fsc: FSContext): Elem = rerenderer.rerenderer.render()

  def rerender()(implicit fsc: FSContext): Js = rerenderer.rerenderer.rerender()

  def renderTableWrapper()(implicit fsc: FSContext, rowsWithIds: Seq[(String, R)], columnsWithIds: Seq[(String, C)]): Elem = {
    implicit lazy val tableRenderer: TableRerenderer = TableRerenderer(JS.rerenderable(implicit rerenderer => implicit fsc => renderTable(), idOpt = Some(tableId), debugLabel = Some("table")))
    <div></div>.apply(tableRenderer.rerenderer.render()).pipe(transformTableWrapperElem)
  }

  def tableFootEnabled: Boolean = false

  def renderTable()(implicit fsc: FSContext, rowsWithIds: Seq[(String, R)], columnsWithIds: Seq[(String, C)], tableRenderer: TableRerenderer): Elem = {

    implicit lazy val tableHeadRerenderer: TableHeadRerenderer = TableHeadRerenderer(
      JS.rerenderableP[(TableHeadRerenderer, TableBodyRerenderer, TableFootRerenderer)](
        implicit rerenderer =>
          implicit fsc => { case (tableHeadRerenderer, tableBodyRerenderer, tableFootRerenderer) =>
            implicit val _tableHeadRerenderer = tableHeadRerenderer
            implicit val _tableBodyRerenderer = tableBodyRerenderer
            implicit val _tableFootRerenderer = tableFootRerenderer
            renderTableHead()
          },
        idOpt = Some(theadId),
        debugLabel = Some("table_head")
      )
    )

    implicit lazy val tableBodyRerenderer: TableBodyRerenderer = TableBodyRerenderer(
      JS.rerenderableP[(TableHeadRerenderer, TableBodyRerenderer, TableFootRerenderer)](
        implicit rerenderer =>
          implicit fsc => { case (tableHeadRerenderer, tableBodyRerenderer, tableFootRerenderer) =>
            implicit val _tableHeadRerenderer = tableHeadRerenderer
            implicit val _tableBodyRerenderer = tableBodyRerenderer
            implicit val _tableFootRerenderer = tableFootRerenderer
            renderTableBody()
          },
        idOpt = Some(tbodyId),
        debugLabel = Some("table_body")
      )
    )

    implicit lazy val tableFootRerenderer: TableFootRerenderer = TableFootRerenderer(
      JS.rerenderableP[(TableHeadRerenderer, TableBodyRerenderer, TableFootRerenderer)](
        implicit rerenderer =>
          implicit fsc => { case (tableHeadRerenderer, tableBodyRerenderer, tableFootRerenderer) =>
            implicit val _tableHeadRerenderer = tableHeadRerenderer
            implicit val _tableBodyRerenderer = tableBodyRerenderer
            implicit val _tableFootRerenderer = tableFootRerenderer
            renderTableFoot()
          },
        idOpt = Some(tfootId),
        debugLabel = Some("table_foot")
      )
    )

    <table></table>.apply {
      tableHeadRerenderer.rerenderer.render((tableHeadRerenderer, tableBodyRerenderer, tableFootRerenderer)) ++
        tableBodyRerenderer.rerenderer.render((tableHeadRerenderer, tableBodyRerenderer, tableFootRerenderer)) ++
        (if (tableFootEnabled) tableFootRerenderer.rerenderer.render((tableHeadRerenderer, tableBodyRerenderer, tableFootRerenderer)) else NodeSeq.Empty)
    }.pipe(transformTableElem)
  }

  def renderTableHead()(implicit
      fsc: FSContext,
      rowsWithIds: Seq[(String, R)],
      columnsWithIds: Seq[(String, C)],
      tableRenderer: TableRerenderer,
      tableHeadRerenderer: TableHeadRerenderer,
      tableBodyRerenderer: TableBodyRerenderer,
      tableFootRerenderer: TableFootRerenderer
  ): Elem = {
    <thead></thead>.apply {
      val rerenderer: Rerenderer = JS.rerenderable(
        rerenderer =>
          implicit fsc => {
            implicit val trRerenderer = TrRerenderer(rerenderer)
            renderTableHeadTr()
          },
        idOpt = Some(theadTrId),
        debugLabel = Some("thead_tr")
      )
      implicit val trRerenderer = TrRerenderer(rerenderer)
      renderTableHeadTRPrepend() ++
        rerenderer.render() ++
        renderTableHeadTRAppend()
    }.pipe(transformTableHeadElem)
  }

  def renderTableHeadTRPrepend()(implicit
      fsc: FSContext,
      rowsWithIds: Seq[(String, R)],
      columnsWithIds: Seq[(String, C)],
      tableRenderer: TableRerenderer,
      tableHeadRerenderer: TableHeadRerenderer,
      tableBodyRerenderer: TableBodyRerenderer,
      tableFootRerenderer: TableFootRerenderer,
      trRerenderer: TrRerenderer
  ): NodeSeq = NodeSeq.Empty

  def renderTableHeadTRAppend()(implicit
      fsc: FSContext,
      rowsWithIds: Seq[(String, R)],
      columnsWithIds: Seq[(String, C)],
      tableRenderer: TableRerenderer,
      tableHeadRerenderer: TableHeadRerenderer,
      tableBodyRerenderer: TableBodyRerenderer,
      tableFootRerenderer: TableFootRerenderer,
      trRerenderer: TrRerenderer
  ): NodeSeq = NodeSeq.Empty

  def renderTableHeadTr()(implicit
      fsc: FSContext,
      rowsWithIds: Seq[(String, R)],
      columnsWithIds: Seq[(String, C)],
      tableRenderer: TableRerenderer,
      tableHeadRerenderer: TableHeadRerenderer,
      tableBodyRerenderer: TableBodyRerenderer,
      tableFootRerenderer: TableFootRerenderer,
      trRerenderer: TrRerenderer
  ): Elem = {
    <tr></tr>.apply {
      columnsWithIds.zipWithIndex.map({ case ((colThId, col), colIdx) =>
        JS.rerenderable(
          implicit rerenderer =>
            implicit fsc => {
              implicit val thRerenderer: ThRerenderer = ThRerenderer(rerenderer)
              implicit val _col: C = col
              implicit val _tableColIdx: TableColIdx = new TableColIdx(colIdx)
              implicit val _colThId: TableColId = new TableColId(colThId)
              renderTableHeadTrTh().pipe(transformTableHeadTrThElem)
            },
          debugLabel = Some(s"thead_tr_td${colIdx}")
        ).render()
      }).mkNS
    }.pipe(transformTableHeadTrElem)
  }

  // ### Table body: ###
  def renderTableBody()(implicit
      fsc: FSContext,
      rowsWithIds: Seq[(String, R)],
      columnsWithIds: Seq[(String, C)],
      tableRenderer: TableRerenderer,
      tableHeadRerenderer: TableHeadRerenderer,
      tableBodyRerenderer: TableBodyRerenderer,
      tableFootRerenderer: TableFootRerenderer
  ): Elem = {
    <tbody></tbody>.apply {
      rowsWithIds.zipWithIndex.map({ case ((rowId, row), rowIdx) =>
        implicit val _row: R = row
        implicit val _rowIdx: TableRowIdx = new TableRowIdx(rowIdx)
        implicit val _rowId: TableRowId = new TableRowId(rowId)
        val rerenderer: Rerenderer = JS.rerenderable(
          implicit rerenderer =>
            implicit fsc => {
              implicit val trRerenderer: TrRerenderer = TrRerenderer(rerenderer)
              renderTableBodyTr()
            },
          idOpt = Some(rowId),
          debugLabel = Some(s"tbody_tr${rowIdx}")
        )
        implicit val trRerenderer: TrRerenderer = TrRerenderer(rerenderer)
        renderTableBodyTrPrepend() ++
          rerenderer.render() ++
          renderTableBodyTrAppend()
      }).mkNS
    }.pipe(transformTableBodyElem)
  }

  def renderTableBodyTrPrepend()(implicit
      fsc: FSContext,
      rowsWithIds: Seq[(String, R)],
      columnsWithIds: Seq[(String, C)],
      tableRenderer: TableRerenderer,
      tableHeadRerenderer: TableHeadRerenderer,
      tableBodyRerenderer: TableBodyRerenderer,
      tableFootRerenderer: TableFootRerenderer,
      trRerenderer: TrRerenderer,
      row: R,
      rowIdx: TableRowIdx,
      rowId: TableRowId
  ): NodeSeq = NodeSeq.Empty

  def renderTableBodyTr()(implicit
      fsc: FSContext,
      rowsWithIds: Seq[(String, R)],
      columnsWithIds: Seq[(String, C)],
      tableRenderer: TableRerenderer,
      tableHeadRerenderer: TableHeadRerenderer,
      tableBodyRerenderer: TableBodyRerenderer,
      tableFootRerenderer: TableFootRerenderer,
      trRerenderer: TrRerenderer,
      row: R,
      rowIdx: TableRowIdx,
      rowId: TableRowId
  ): Elem = {
    <tr></tr>.apply {
      columnsWithIds.zipWithIndex.map({ case ((colThId, col), colIdx) =>
        JS.rerenderable(
          implicit rerenderer =>
            implicit fsc => {
              implicit val tdRerenderer: TdRerenderer = TdRerenderer(rerenderer)
              implicit val _col: C = col
              implicit val _tableColIdx: TableColIdx = new TableColIdx(colIdx)
              implicit val _colId: TableColId = new TableColId(colThId)
              renderTableBodyTrTd().pipe(transformTableBodyTrTdElem)
            },
          debugLabel = Some(s"table_row_${rowIdx.idx}_col_${colIdx}")
        ).render()
      }).mkNS
    }.pipe(transformTableBodyTrElem)
  }

  def renderTableBodyTrAppend()(implicit
      fsc: FSContext,
      rowsWithIds: Seq[(String, R)],
      columnsWithIds: Seq[(String, C)],
      tableRenderer: TableRerenderer,
      tableHeadRerenderer: TableHeadRerenderer,
      tableBodyRerenderer: TableBodyRerenderer,
      tableFootRerenderer: TableFootRerenderer,
      trRerenderer: TrRerenderer,
      row: R,
      rowIdx: TableRowIdx,
      rowId: TableRowId
  ): NodeSeq = NodeSeq.Empty

  // ### Table foot: ###

  def renderTableFoot()(implicit
      fsc: FSContext,
      rowsWithIds: Seq[(String, R)],
      columnsWithIds: Seq[(String, C)],
      tableRenderer: TableRerenderer,
      tableHeadRerenderer: TableHeadRerenderer,
      tableBodyRerenderer: TableBodyRerenderer,
      tableFootRerenderer: TableFootRerenderer
  ): Elem = {
    <tfoot></tfoot>.apply {
      val rerenderer: Rerenderer = JS.rerenderable(
        rerenderer =>
          implicit fsc => {
            implicit val trRerenderer = TrRerenderer(rerenderer)
            renderTableFootTr()
          },
        idOpt = Some(tfootTrId),
        debugLabel = Some("tfoot_tr")
      )
      implicit val trRerenderer = TrRerenderer(rerenderer)
      renderTableFootTRPrepend() ++
        rerenderer.render() ++
        renderTableFootTRAppend()
    }.pipe(transformTableFootElem)
  }

  def renderTableFootTRPrepend()(implicit
      fsc: FSContext,
      rowsWithIds: Seq[(String, R)],
      columnsWithIds: Seq[(String, C)],
      tableRenderer: TableRerenderer,
      tableHeadRerenderer: TableHeadRerenderer,
      tableBodyRerenderer: TableBodyRerenderer,
      tableFootRerenderer: TableFootRerenderer,
      trRerenderer: TrRerenderer
  ): NodeSeq = NodeSeq.Empty

  def renderTableFootTRAppend()(implicit
      fsc: FSContext,
      rowsWithIds: Seq[(String, R)],
      columnsWithIds: Seq[(String, C)],
      tableRenderer: TableRerenderer,
      tableHeadRerenderer: TableHeadRerenderer,
      tableBodyRerenderer: TableBodyRerenderer,
      tableFootRerenderer: TableFootRerenderer,
      trRerenderer: TrRerenderer
  ): NodeSeq = NodeSeq.Empty

  def renderTableFootTr()(implicit
      fsc: FSContext,
      rowsWithIds: Seq[(String, R)],
      columnsWithIds: Seq[(String, C)],
      tableRenderer: TableRerenderer,
      tableHeadRerenderer: TableHeadRerenderer,
      tableBodyRerenderer: TableBodyRerenderer,
      tableFootRerenderer: TableFootRerenderer,
      trRerenderer: TrRerenderer
  ): Elem = {
    <tr></tr>.apply {
      columnsWithIds.zipWithIndex.map({ case ((colThId, col), colIdx) =>
        JS.rerenderable(
          implicit rerenderer =>
            implicit fsc => {
              implicit val thRerenderer: ThRerenderer = ThRerenderer(rerenderer)
              implicit val _colThId: TableColId = new TableColId(colThId)
              implicit val _col: C = col
              implicit val _tableColIdx: TableColIdx = new TableColIdx(colIdx)

              renderTableFootTrTh().pipe(transformTableFootTrThElem)
            },
          debugLabel = Some(s"tfoot_tr_td${colIdx}")
        ).render()
      }).mkNS
    }.pipe(transformTableFootTrElem)
  }
}
