package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.bootstrap5.components.BSBtnDropdown
import com.fastscala.components.bootstrap5.utils.BSBtn
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS
import com.fastscala.scala_xml.rerenderers.{Rerenderer, RerendererP}

trait Table6SelectableRowsWithActions extends Table6SelectableRows with Table6StdColsHelper {

  def actionsForRows(rows: Set[R]): Seq[BSBtn] = Nil

  def actionsBtnToIncludeInTopDropdown: BSBtn = BSBtn().BtnPrimary.lbl("Actions")

  def actionsBtnToIncludeInRowDropdown: BSBtn = BSBtn().BtnPrimary.lbl("Actions").sm

  override def onSelectedRowsChange()(implicit fsc: FSContext): Js = super.onSelectedRowsChange() &
    actionsDropdownBtnRenderer.rerender()

  lazy val actionsDropdownBtnRenderer: Rerenderer = JS.rerenderable(rerenderer => implicit fsc => {
    BSBtnDropdown(actionsBtnToIncludeInTopDropdown)(
      actionsForRows(selectedVisibleRows): _*
    )
  }, debugLabel = Some("actions_dropdown_btn"))

  lazy val ColActionsDefault = ColNsFullTd(actionsBtnToIncludeInRowDropdown.content.toString(), implicit fsc => {
    case (rowsWithIds, columnsWithIds, tableWrapperRenderer, tableRenderer, tableHeadRerenderer, tableBodyRerenderer, tableFootRerenderer, trRerenderer, tdRerenderer, col, colIdx, colId, row, rowIdx, rowId) =>
      val contents = BSBtnDropdown(actionsBtnToIncludeInRowDropdown)(
        actionsForRows(Set(row)): _*
      )
      <td class="py-1">{contents}</td>
  })
}
