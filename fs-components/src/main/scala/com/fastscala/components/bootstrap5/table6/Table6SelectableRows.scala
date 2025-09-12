package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.bootstrap5.utils.{BSBtn, ImmediateInputFields}
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem

trait Table6SelectableRows extends Table6Base with Table6ColsLabeled with Table6StandardColumns {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  lazy val allSelectedRowsEvenIfNotVisible = collection.mutable.Set[R]()

  def selectedVisibleRows: Set[R] = rows(rowsHints())._1.toSet intersect allSelectedRowsEvenIfNotVisible.toSet

  def transformSelectedRowTd(td: Elem): Elem = td.bg_primary_subtle

  override def transformTableBodyTrTdElem(
    elem: Elem
  )(implicit fsc: FSContext, ctx: Ctx, columns: Seq[(String, C)], rows: Seq[(String, R)], tableBodyRerenderer: TableBodyRerenderer, trRerenderer: TrRerenderer, col: C, colIdx: TableColIdx, colId: TableColId, row: R, rowIdx: TableRowIdx, rowId: TableRowId): Elem = {
    super.transformTableBodyTrTdElem(elem)
      .pipe(elem => if (allSelectedRowsEvenIfNotVisible.contains(row)) transformSelectedRowTd(elem) else elem)
      .pipe(elem =>
        col match {
          case ColSelectRow => elem.align_middle.text_center
          case _            => elem
        }
      )
  }

  def onSelectedRowsChange()(implicit fsc: FSContext): Js = JS.void

  def selectAllVisibleRowsBtn: BSBtn = BSBtn().BtnOutlinePrimary.lbl(s"Select All").ajax(implicit fsc => {
    allSelectedRowsEvenIfNotVisible.clear()
    allSelectedRowsEvenIfNotVisible ++= rows(rowsHints())._1
    onSelectedRowsChange() &
      rerender()
  })

  def clearRowSelectionBtn: BSBtn = BSBtn().BtnOutlinePrimary.lbl(s"Clear Selection").ajax(implicit fsc => {
    allSelectedRowsEvenIfNotVisible.clear()
    onSelectedRowsChange() &
      rerender()
  })

  def onRowSelectionChanged(trRerenderer: TrRerenderer)(implicit fsc: FSContext): Js = trRerenderer.rerenderer.rerender()

  val ColSelectRow = new Table6StandardColumn[R, C, Ctx] {

    override def label: String = ""

    override def renderTheadTrTh()(implicit tableHeadRerenderer: TableHeadRerenderer, trRerenderer: TrRerenderer, thRerenderer: ThRerenderer, colIdx: TableColIdx, pageRows: Seq[(String, R)], fsc: FSContext): Elem = <th></th>

    override def renderTbodyTrTd()(implicit tableBodyRerenderer: TableBodyRerenderer, trRerenderer: TrRerenderer, tdRerenderer: TdRerenderer, value: R, rowIdx: TableRowIdx, colIdx: TableColIdx, rows: Seq[(String, R)], fsc: FSContext): Elem = {
      val contents = ImmediateInputFields.checkbox(
        () => allSelectedRowsEvenIfNotVisible.contains(value),
        selected => {
          if (selected) allSelectedRowsEvenIfNotVisible += value
          else allSelectedRowsEvenIfNotVisible -= value
          onSelectedRowsChange() &
            onRowSelectionChanged(trRerenderer)
        },
        ""
      ).m_0.d_inline_block
      <td>{contents}</td>
    }
  }
}
