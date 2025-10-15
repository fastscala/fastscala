package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.bootstrap5.helpers.BSHelpers.RichElemBootstrapClasses
import com.fastscala.components.bootstrap5.utils.{BSBtn, ImmediateInputFields}
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem

trait Table6SortableRows extends Table6Base {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  def isSortableRow(row: R): Boolean = true

  def sortedRow(row: R, newIdx: Int): Js = Js.Void

  def sortableRowsHandle: Option[String] = None

  override def transformTableBodyTrElem(elem: Elem)(implicit fsc: FSContext, columns: Seq[(String, C)], rows: Seq[(String, R)], tableBodyRerenderer: TableBodyRerenderer, trRerenderer: TrRerenderer, row: R, rowIdx: TableRowIdx, rowId: TableRowId): Elem = {
    val transformed = super.transformTableBodyTrElem(elem)
    if (isSortableRow(row)) transformed.addClass("sortable-row").withAttr("on-sorted" -> fsc.callback(Js("idx"), newIdx => sortedRow(row, newIdx.toInt)).cmd) else transformed
  }

  def sortableRowsMinDistancePx = 15

  override def renderTableBody()(implicit
    fsc: FSContext,
    rowsWithIds: Seq[(String, R)],
    columnsWithIds: Seq[(String, C)],
    tableRenderer: TableRerenderer,
    tableHeadRerenderer: TableHeadRerenderer,
    tableBodyRerenderer: TableBodyRerenderer,
    tableFootRerenderer: TableFootRerenderer
  ): (Elem, Js) = super.renderTableBody() match {
    case (elem, js) =>
      elem -> (js & Js(s"""$$(${JS.asJsStr("#" + tbodyId)}).sortable({
           |  items: 'tr.sortable-row',
           |  distance: $sortableRowsMinDistancePx,
           |  ${sortableRowsHandle.map(sortableRowsHandle => s"""handle: ${JS.asJsStr(sortableRowsHandle)},""").getOrElse("")}
           |  update: function(event, ui) {
           |    (0,eval)('var idx = ' + ui.item.index() + ';' + ui.item.attr('on-sorted'));
           |  },
           |  helper: function(e, ui) {
           |    ui.children().each(function() {
           |      $$(this).width($$(this).width());
           |    });
           |    return ui;
           |  }
           |})
           |""".stripMargin))
  }

}
