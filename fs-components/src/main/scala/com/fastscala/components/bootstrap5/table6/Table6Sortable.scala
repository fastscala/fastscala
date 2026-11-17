package com.fastscala.components.bootstrap5.table6

import com.fastscala.core.{FSContext, FSPage}
import com.fastscala.js.Js
import com.fastscala.scala_xml.xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS
import com.fastscala.utils.LazyWithPage

import scala.xml.Elem

trait Table6Sortable extends Table6Base with Table6StandardColumns {

  type SortAsc = Boolean

  def defaultSortCol(implicit page: FSPage): Option[C] = None

  def defaultSortColFinal(implicit page: FSPage): Option[C] = defaultSortCol

  def defaultSortAsc(col: C)(implicit page: FSPage): SortAsc = true

  def defaultSortAscFinal(col: C)(implicit page: FSPage): SortAsc = defaultSortAsc(col)

  def isSortable(col: C): Boolean

  lazy val currentSortCol: LazyWithPage[Option[C]] = LazyWithPage(implicit fsPage => defaultSortColFinal)
  lazy val currentSortAsc: LazyWithPage[SortAsc] = LazyWithPage(implicit fsPage => currentSortCol().map(defaultSortAscFinal).getOrElse(true))

  override def rowsHints()(implicit fsc: FSContext): Seq[RowsHint] = super.rowsHints() ++ currentSortCol()(using fsc.page).map(col => {
    SortingRowsHint(col, currentSortAsc()(using fsc.page))
  }).toList

  def clickedClientSide()(
    implicit
    fsc: FSContext,
    rowsWithIds: Seq[(String, R)],
    columnsWithIds: Seq[(String, C)],
    knownTotalNumberOfRows: KnownTotalNumberOfRowsMaterialized,
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
  ): Js = {
    fsc.callback(() => onSortColClickedServerSide(col))
  }

  /** Sorts by `col` (or, if it's sorted by it already, in the other direction), and rerenders the table. */
  def onSortColClickedServerSide(col: C)(implicit fsc: FSContext): Js = {
    if (currentSortCol()(using fsc.page) == Some(col)) {
      currentSortAsc() = !currentSortAsc()(using fsc.page)
    } else {
      currentSortCol() = Some(col)
      currentSortAsc() = defaultSortAscFinal(col)(using fsc.page)
    }
    rerender()
  }

  override def renderTableHeadTrTh()(
    implicit
    fsc: FSContext,
    rowsWithIds: Seq[(String, R)],
    columnsWithIds: Seq[(String, C)],
    knownTotalNumberOfRows: KnownTotalNumberOfRowsMaterialized,
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
  ): Elem = {
    val elem = super.renderTableHeadTrTh()
    ({
      if (isSortable(col)) {
        val chevron =
          if (currentSortCol()(using fsc.page) == Some(col)) (if (currentSortAsc()(using fsc.page)) "bi-chevron-double-down" else "bi-chevron-double-up")
          else "bi-chevron-expand"
        elem.withAppendedToContents(<i class={s"bi $chevron"} style="float: right;padding: 0;"></i>)
      } else {
        elem
      }
    }).withAttr("onclick")(old => {
      clickedClientSide().cmd
    })
  }
}
