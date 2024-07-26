package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.{FSContext, FSXmlEnv}
import com.fastscala.js.Js
import com.fastscala.js.rerenderers.Rerenderer
import com.fastscala.templates.bootstrap5.components.BSBtnDropdown
import com.fastscala.templates.bootstrap5.utils.BSBtn

trait Table5SelectableRowsWithActions[E <: FSXmlEnv] extends Table5SelectableRows[E] {

  def actionsForRows(rows: Set[R]): Seq[BSBtn[E]] = Nil

  def actionsBtnToIncludeInDropdown: BSBtn[E] = BSBtn().BtnPrimary.lbl("Actions")

  override def onSelectedRowsChange()(implicit fsc: FSContext): Js = super.onSelectedRowsChange() &
    actionsDropdownBtnRenderer.rerender()


  lazy val actionsDropdownBtnRenderer: Rerenderer[E] = Js.rerenderable(rerenderer => implicit fsc => {
    BSBtnDropdown(actionsBtnToIncludeInDropdown)(
      actionsForRows(selectedVisibleRows): _*
    )
  }, debugLabel = Some("actions_dropdown_btn"))
}
