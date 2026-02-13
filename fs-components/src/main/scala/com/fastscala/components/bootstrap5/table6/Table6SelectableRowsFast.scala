package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.bootstrap5.utils.{BSBtn, ImmediateInputFields}
import com.fastscala.core.FSContext
import com.fastscala.js.{Js, JsFunc1}
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.{JS, printBeforeExec}

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem

trait Table6SelectableRowsFast extends Table6SelectableRowsBase with Table6ColsLabeled with Table6StandardColumns with Table6RowsWithStableId {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  lazy val allSelectedRowsEvenIfNotVisible = collection.mutable.Set[R]()

  def selectedVisibleRows: Seq[R] =
    if (allSelectedRowsEvenIfNotVisible.nonEmpty) rows(rowsHints()).filter(row => allSelectedRowsEvenIfNotVisible.contains(row))
    else Nil

  def selectedRowClass: String

  override def transformTableBodyTrElem(elem: Elem)(implicit
                                                    fsc: FSContext,
                                                    columns: Seq[(String, C)],
                                                    rows: Seq[(String, R)],
                                                    knownTotalNumberOfRows: Option[Int],
                                                    tableBodyRerenderer: TableBodyRerenderer,
                                                    trRerenderer: TrRerenderer,
                                                    row: R,
                                                    rowIdx: TableRowIdx,
                                                    rowId: TableRowId
  ): Elem = if (allSelectedRowsEvenIfNotVisible.contains(row)) elem.addClass(selectedRowClass) else elem

  override def transformTableBodyTrTdElem(
                                           elem: Elem
                                         )(implicit
                                           fsc: FSContext,
                                           columns: Seq[(String, C)],
                                           rows: Seq[(String, R)],
                                           knownTotalNumberOfRows: Option[Int],
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
      .pipe(elem =>
        col match {
          case ColSelectRow => elem.align_middle.text_center
          case _ => elem
        }
      )
  }

  def selectAllVisibleRowsBtn: BSBtn = BSBtn().BtnOutlinePrimary.lbl(i18n_selectAll).callback(implicit fsc => {
    allSelectedRowsEvenIfNotVisible.clear()
    allSelectedRowsEvenIfNotVisible ++= rows(rowsHints())
    onSelectedRowsChange() &
      JS.removeClassFromElemsMatchingSelector(s"#$tbodyId tr", selectedRowClass) &
      JS.forEachQuerySelector(s"#$tbodyId tr input[select-row-for-table=\"${tableId}\"]", JsFunc1(js => Js(s"$js.checked = false"))) &
      JS.addClassToElemsMatchingSelector(allSelectedRowsEvenIfNotVisible.map(r => "#" + idForRow(r, -1)).mkString(", "), selectedRowClass) &
      JS.forEachQuerySelector(allSelectedRowsEvenIfNotVisible.map(r => "#" + idForRow(r, -1) + s" input[select-row-for-table=\"${tableId}\"]").mkString(", "), JsFunc1(js => Js(s"$js.checked = true")))
  })

  def clearRowSelectionBtn: BSBtn = BSBtn().BtnOutlinePrimary.lbl(i18n_clearSelection).callback(implicit fsc => {
    allSelectedRowsEvenIfNotVisible.clear()
    onSelectedRowsChange() &
      JS.removeClassFromElemsMatchingSelector(s"#$tbodyId tr", selectedRowClass) &
      JS.forEachQuerySelector(s"#$tbodyId tr input[select-row-for-table=\"${tableId}\"]", JsFunc1(js => Js(s"$js.checked = false")))
  })

  def onRowSelectionChanged()(
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
    tdRerenderer: TdRerenderer,
    col: C,
    colIdx: TableColIdx,
    colId: TableColId,
    row: R,
    rowIdx: TableRowIdx,
    rowId: TableRowId
  ): Js = Js.Void

  val ColSelectRow = new Table6StandardColumn[R] {

    override def label: String = ""

    override def renderTheadTrTh()(
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
    ): Elem = <th></th>

    override def renderTbodyTrTd()(
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
          (if (selected) {
            allSelectedRowsEvenIfNotVisible += row
            JS.addClass(idForRow(row, -1), selectedRowClass)
          } else {
            allSelectedRowsEvenIfNotVisible -= row
            JS.removeClass(idForRow(row, -1), selectedRowClass)
          }) &
            onSelectedRowsChange() &
            onRowSelectionChanged()
        },
        "",
        additionalAttrs = List("select-row-for-table" -> tableId)
      ).m_0.d_inline_block
      <td>
        {contents}
      </td>
    }

    override def renderTfootTrTh()(
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
    ): Elem = <th></th>
  }
}
