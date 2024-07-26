package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.{FSContext, FSXmlEnv}
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.utils.{BSBtn, ImmediateInputFields}
import com.fastscala.xml.scala_xml.FSScalaXmlSupport.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem

trait Table5SelectableRows[E <: FSXmlEnv] extends Table5Base[E] with Table5ColsLabeled {

  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

  lazy val allSelectedRowsEvenIfNotVisible = collection.mutable.Set[R]()

  def selectedVisibleRows: Set[R] = rows(rowsHints()).toSet intersect allSelectedRowsEvenIfNotVisible.toSet

  override def transformTRTDElem(elem: E#Elem)(implicit tableBodyRerenderer: TableBodyRerenderer[E], trRerenderer: TRRerenderer[E], col: C, value: R, rowIdx: TableRowIdx, columns: Seq[(String, C)], rows: Seq[(String, R)], fsc: FSContext): E#Elem = {
    super.transformTRTDElem(elem)
      .pipe(elem => if (allSelectedRowsEvenIfNotVisible.contains(value)) elem.bg_primary_subtle else elem)
      .pipe(elem => col match {
        case ColSelectRow => elem.align_middle.text_center
        case _ => elem
      })
  }

  def onSelectedRowsChange()(implicit fsc: FSContext): Js = Js.void

  def selectAllVisibleRowsBtn: BSBtn[E] = BSBtn().BtnOutlinePrimary.lbl(s"Select All").ajax(implicit fsc => {
    allSelectedRowsEvenIfNotVisible.clear()
    allSelectedRowsEvenIfNotVisible ++= rows(rowsHints())
    onSelectedRowsChange() &
      rerenderTableAround()
  })

  def clearRowSelectionBtn: BSBtn[E] = BSBtn().BtnOutlinePrimary.lbl(s"Clear Selection").ajax(implicit fsc => {
    allSelectedRowsEvenIfNotVisible.clear()
    onSelectedRowsChange() &
      rerenderTableAround()
  })

  val ColSelectRow = new Table5StandardColumn[E, R] {

    override def label: String = ""

    override def renderTH()(implicit tableHeadRerenderer: TableHeadRerenderer[E], trRerenderer: TRRerenderer[E], thRerenderer: THRerenderer[E], colIdx: TableColIdx, pageRows: Seq[(String, R)], fsc: FSContext): E#Elem = <th></th>.asFSXml()

    override def renderTD()(implicit tableBodyRerenderer: TableBodyRerenderer[E], trRerenderer: TRRerenderer[E], tdRerenderer: TDRerenderer[E], value: R, rowIdx: TableRowIdx, colIdx: TableColIdx, rows: Seq[(String, R)], fsc: FSContext): E#Elem = {
      val contents = ImmediateInputFields.checkbox(() => allSelectedRowsEvenIfNotVisible.contains(value), selected => {
        if (selected) allSelectedRowsEvenIfNotVisible += value
        else allSelectedRowsEvenIfNotVisible -= value
        onSelectedRowsChange() &
          trRerenderer.rerenderer.rerender()
      }, "").m_0.d_inline_block
      <td>{contents}</td>.asFSXml()
    }
  }
}
