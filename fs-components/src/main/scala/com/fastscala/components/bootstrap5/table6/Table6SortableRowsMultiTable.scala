package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.bootstrap5.helpers.BSHelpers.RichElemBootstrapClasses
import com.fastscala.components.bootstrap5.utils.{BSBtn, ImmediateInputFields}
import com.fastscala.core.FSContext
import com.fastscala.core.circe.CirceSupport.FSContextWithCirceSupport
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS
import io.circe.Decoder
import io.circe.generic.semiauto

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem

trait Table6SortableRowsMultiTable extends Table6Base with Table6RowsWithId {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  def isSortableRow(row: R): Boolean = true

  def sortedRow(row: R, newIdx: Int): Js = Js.Void

  def sortableRowsHandle: Option[String] = None

  def sortableRowsConnectWith: Option[String] = None

  case class RowSorted(newIdx: Int, rowId: String)

  implicit val jsonDecoder: Decoder[RowSorted] = semiauto.deriveDecoder[RowSorted]

  override def transformTableBodyTrElem(elem: Elem)(implicit fsc: FSContext, columns: Seq[(String, C)], rows: Seq[(String, R)], tableBodyRerenderer: TableBodyRerenderer, trRerenderer: TrRerenderer, row: R, rowIdx: TableRowIdx, rowId: TableRowId): Elem = {
    val transformed = super.transformTableBodyTrElem(elem)
    if (isSortableRow(row)) transformed.addClass("sortable-row").withAttr("row-id", getIdForRow(row)) else transformed
  }

  def sortableRowsMinDistancePx = 15

  override def renderTableBody()(implicit fsc: FSContext, rowsWithIds: Seq[(String, R)], columnsWithIds: Seq[(String, C)], tableRenderer: TableRerenderer, tableHeadRerenderer: TableHeadRerenderer, tableBodyRerenderer: TableBodyRerenderer, tableFootRerenderer: TableFootRerenderer): (Elem, Js) =
    super.renderTableBody() match {
      case (elem, js) => {
        val callback = fsc.callbackJSONDecoded[RowSorted](
          Js("""{"newIdx": idx, "rowId": rowId}"""),
          { case RowSorted(newIdx, rowId) =>
            getRowForId(rowId) match {
              case Some(row) => sortedRow(row, newIdx)
              case None      => throw new Exception(s"Invalid id for row ${rowId}")
            }
          }
        ).cmd
        elem -> (js & Js(s"""$$(${JS.asJsStr("#" + tbodyId)}).sortable({
                            |  items: 'tr.sortable-row',
                            |  distance: $sortableRowsMinDistancePx,
                            |  ${sortableRowsHandle.map(sortableRowsHandle => s"""handle: ${JS.asJsStr(sortableRowsHandle)},""").getOrElse("")}
                            |  ${sortableRowsConnectWith.map(sortableRowsConnectWith => s"""connectWith: ${JS.asJsStr(sortableRowsConnectWith)},""").getOrElse("")}
                            |  update: function(event, ui) {
                            |    console.log(event);
                            |    console.log(ui);
                            |    window.myui = ui;
                            |    var idx = ui.item.index();
                            |    var rowId = ui.item.attr('row-id');
                            |    if (${JS.elementById(tableId)}.contains(ui.item[0])) {
                            |      $callback
                            |    }
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

}
