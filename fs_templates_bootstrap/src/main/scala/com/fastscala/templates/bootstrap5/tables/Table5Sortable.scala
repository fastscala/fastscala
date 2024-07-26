package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.{FSContext, FSXmlEnv}
import com.fastscala.js.Js
import com.fastscala.utils.Lazy
import com.fastscala.xml.scala_xml.FSScalaXmlSupport.RichElem

trait Table5Sortable[E <: FSXmlEnv] extends Table5Base[E] with Table5StandardColumns[E] {

  import com.fastscala.core.FSXmlUtils._

  def defaultSortCol: Option[C] = None

  def defaultSortAsc(col: C): Boolean = true

  def isSortable(col: C): Boolean

  lazy val currentSortCol: Lazy[Option[C]] = Lazy(defaultSortCol)
  lazy val currentSortAsc: Lazy[Boolean] = Lazy(currentSortCol().map(defaultSortAsc).getOrElse(true))

  override def rowsHints(): Seq[RowsHint] = super.rowsHints() ++ currentSortCol().map(col => {
    SortingRowsHint[C](col, currentSortAsc())
  }).toSeq

  def clickedClientSide()(
    implicit tableHeadRerenderer: TableHeadRerenderer[E],
    trRerenderer: TRRerenderer[E],
    thRerenderer: THRerenderer[E],
    columns: Seq[(String, Table5StandardColumn[E, R])],
    rows: Seq[(String, R)],
    colThId: String,
    col: Table5StandardColumn[E, R],
    tableColIdx: TableColIdx,
    fsc: FSContext
  ): Js = {
    fsc.callback(() => {
      if (currentSortCol() == Some(col)) {
        currentSortAsc() = !currentSortAsc()
      } else {
        currentSortCol() = Some(col)
        currentSortAsc() = defaultSortAsc(col)
      }
      tableRenderer.rerenderer.rerender()
    })
  }

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
  ): E#Elem = {
    val elem = super.renderTableHeadTRTH()
    ({
      if (isSortable(col)) {
        val chevron =
          if (currentSortCol() == Some(col)) (if (currentSortAsc()) "bi-chevron-double-down" else "bi-chevron-double-up")
          else "bi-chevron-expand"
        elem.withAppendedToContents(<i class={s"bi $chevron"} style="float: right;padding: 0;"></i>.asFSXml())
      } else {
        elem
      }
    }).withAttr("onclick")(old => {
      clickedClientSide().cmd
    })
  }
}
