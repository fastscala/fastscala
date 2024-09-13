package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.components.BSBtnDropdown
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.xml.scala_xml.JS
import com.fastscala.xml.scala_xml.JS.ScalaXmlRerenderer

trait Table5SelectableRowsWithActions extends Table5SelectableRows with Table5StdColsHelper {

  def actionsForRows(rows: Set[R]): Seq[BSBtn] = Nil

  def actionsBtnToIncludeInTopDropdown: BSBtn = BSBtn().BtnPrimary.lbl("Actions")

  def actionsBtnToIncludeInRowDropdown: BSBtn = BSBtn().BtnPrimary.lbl("Actions").sm

  override def onSelectedRowsChange()(implicit fsc: FSContext): Js = super.onSelectedRowsChange() &
    actionsDropdownBtnRenderer.rerender()

  lazy val actionsDropdownBtnRenderer: ScalaXmlRerenderer = JS.rerenderable(rerenderer => implicit fsc => {
    BSBtnDropdown(actionsBtnToIncludeInTopDropdown)(
      actionsForRows(selectedVisibleRows): _*
    )
  }, debugLabel = Some("actions_dropdown_btn"))

  lazy val DefaultColActions = ColNs(actionsBtnToIncludeInRowDropdown.content.toString(), implicit fsc => row => {
    BSBtnDropdown(actionsBtnToIncludeInRowDropdown)(
      actionsForRows(Set(row)): _*
    )
  })
}
