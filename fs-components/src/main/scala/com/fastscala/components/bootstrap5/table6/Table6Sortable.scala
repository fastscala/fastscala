package com.fastscala.components.bootstrap5.table6

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS
import com.fastscala.utils.Lazy

import scala.xml.Elem

trait Table6Sortable extends Table6Base with Table6StandardColumns {

  def defaultSortCol: Option[C] = None

  def defaultSortAsc(col: C): Boolean = true

  def isSortable(col: C): Boolean

  lazy val currentSortCol: Lazy[Option[C]] = Lazy(defaultSortCol)
  lazy val currentSortAsc: Lazy[Boolean] = Lazy(currentSortCol().map(defaultSortAsc).getOrElse(true))

  override def rowsHints(): Seq[RowsHint] = super.rowsHints() ++ currentSortCol().map(col => {
    SortingRowsHint(col, currentSortAsc())
  }).toList

  def clickedClientSide()(
      implicit
      fsc: FSContext,
      rowsWithIds: Seq[(String, R)],
      columnsWithIds: Seq[(String, C)],
      knownTotalNumberOfRows: Option[Int],
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
    fsc.callback(() => {
      if (currentSortCol() == Some(col)) {
        currentSortAsc() = !currentSortAsc()
      } else {
        currentSortCol() = Some(col)
        currentSortAsc() = defaultSortAsc(col)
      }
      rerender()
    })
  }

  override def renderTableHeadTrTh()(
      implicit
      fsc: FSContext,
      rowsWithIds: Seq[(String, R)],
      columnsWithIds: Seq[(String, C)],
      knownTotalNumberOfRows: Option[Int],
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
          if (currentSortCol() == Some(col)) (if (currentSortAsc()) "bi-chevron-double-down" else "bi-chevron-double-up")
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
