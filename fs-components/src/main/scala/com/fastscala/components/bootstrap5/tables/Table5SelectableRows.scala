package com.fastscala.components.bootstrap5.tables

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.bootstrap5.utils.{BSBtn, ImmediateInputFields}
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem

trait Table5SelectableRows extends Table5Base with Table5ColsLabeled {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  lazy val allSelectedRowsEvenIfNotVisible = collection.mutable.Set[R]()

  def selectedVisibleRows: Set[R] = rows(rowsHints()).toSet intersect allSelectedRowsEvenIfNotVisible.toSet

  def transformSelectedRowTd(td: Elem): Elem = td.bg_primary_subtle

  override def transformTRTDElem(elem: Elem)(implicit tableBodyRerenderer: TableBodyRerenderer, trRerenderer: TRRerenderer, col: C, value: R, rowIdx: TableRowIdx, columns: Seq[(String, C)], rows: Seq[(String, R)], fsc: FSContext): Elem = {
    super.transformTRTDElem(elem)
      .pipe(elem => if (allSelectedRowsEvenIfNotVisible.contains(value)) transformSelectedRowTd(elem) else elem)
      .pipe(elem => col match {
        case ColSelectRow => elem.align_middle.text_center
        case _ => elem
      })
  }

  def onSelectedRowsChange()(implicit fsc: FSContext): Js = JS.void

  def selectAllVisibleRowsBtn: BSBtn = BSBtn().BtnOutlinePrimary.lbl(s"Select All").ajax(implicit fsc => {
    allSelectedRowsEvenIfNotVisible.clear()
    allSelectedRowsEvenIfNotVisible ++= rows(rowsHints())
    onSelectedRowsChange() &
      rerenderTableAround()
  })

  def clearRowSelectionBtn: BSBtn = BSBtn().BtnOutlinePrimary.lbl(s"Clear Selection").ajax(implicit fsc => {
    allSelectedRowsEvenIfNotVisible.clear()
    onSelectedRowsChange() &
      rerenderTableAround()
  })

  def onRowSelectionChanged(trRerenderer: TRRerenderer)(implicit fsc: FSContext): Js = trRerenderer.rerenderer.rerender()

  val ColSelectRow = new Table5StandardColumn[R] {

    override def label: String = ""

    override def renderTH()(implicit tableHeadRerenderer: TableHeadRerenderer, trRerenderer: TRRerenderer, thRerenderer: THRerenderer, colIdx: TableColIdx, pageRows: Seq[(String, R)], fsc: FSContext): Elem = <th></th>

    override def renderTD()(implicit tableBodyRerenderer: TableBodyRerenderer, trRerenderer: TRRerenderer, tdRerenderer: TDRerenderer, value: R, rowIdx: TableRowIdx, colIdx: TableColIdx, rows: Seq[(String, R)], fsc: FSContext): Elem = {
      val contents = ImmediateInputFields.checkbox(() => allSelectedRowsEvenIfNotVisible.contains(value), selected => {
        if (selected) allSelectedRowsEvenIfNotVisible += value
        else allSelectedRowsEvenIfNotVisible -= value
        onSelectedRowsChange() &
          onRowSelectionChanged(trRerenderer)
      }, "").m_0.d_inline_block
      <td>{contents}</td>
    }
  }
}
