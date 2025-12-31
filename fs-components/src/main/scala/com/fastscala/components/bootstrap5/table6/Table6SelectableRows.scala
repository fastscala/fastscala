package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.bootstrap5.utils.{BSBtn, ImmediateInputFields}
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem

trait Table6SelectableRows extends Table6SelectableRowsBase with Table6ColsLabeled with Table6StandardColumns {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  lazy val allSelectedRowsEvenIfNotVisible = collection.mutable.Set[R]()

  def selectedVisibleRows: Seq[R] = rows(rowsHints()).filter(row => allSelectedRowsEvenIfNotVisible.contains(row))

  def transformSelectedRowTd(td: Elem): Elem = td.bg_primary_subtle

  override def transformTableBodyTrTdElem(
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
                                         ): Elem = {
    super.transformTableBodyTrTdElem(elem)
      .pipe(elem => if (allSelectedRowsEvenIfNotVisible.contains(row)) transformSelectedRowTd(elem) else elem)
      .pipe(elem =>
        col match {
          case ColSelectRow => elem.align_middle.text_center
          case _ => elem
        }
      )
  }

  def selectAllVisibleRowsBtn: BSBtn = BSBtn().BtnOutlinePrimary.lbl(s"Select All").ajax(implicit fsc => {
    allSelectedRowsEvenIfNotVisible.clear()
    allSelectedRowsEvenIfNotVisible ++= rows(rowsHints())
    onSelectedRowsChange() &
      rerender()
  })

  def clearRowSelectionBtn: BSBtn = BSBtn().BtnOutlinePrimary.lbl(s"Clear Selection").ajax(implicit fsc => {
    allSelectedRowsEvenIfNotVisible.clear()
    onSelectedRowsChange() &
      rerender()
  })

  def onRowSelectionChanged(trRerenderer: TrRerenderer)(implicit fsc: FSContext): Js = trRerenderer.rerenderer.rerender()

  val ColSelectRow: Table6StandardColumn[R] = new Table6StandardColumn[R] {

    override def label: String = ""

    override def renderTheadTrTh()(
      implicit
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
    ): Elem = <th></th>

    override def renderTbodyTrTd()(
      implicit
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
    ): Elem = {
      val contents = ImmediateInputFields.checkbox(
        () => allSelectedRowsEvenIfNotVisible.contains(row),
        selected => {
          if (selected) allSelectedRowsEvenIfNotVisible += row
          else allSelectedRowsEvenIfNotVisible -= row
          onSelectedRowsChange() &
            onRowSelectionChanged(trRerenderer)
        },
        ""
      ).m_0.d_inline_block
      <td>{contents}</td>
    }

    override def renderTfootTrTh()(
      implicit
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
    ): Elem = <th></th>
  }
}
