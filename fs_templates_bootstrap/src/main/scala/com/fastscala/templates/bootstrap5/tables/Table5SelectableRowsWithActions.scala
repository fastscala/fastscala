package com.fastscala.templates.bootstrap5.tables

import com.fastscala.code.FSContext
import com.fastscala.js.{Js, Rerenderer}
import com.fastscala.templates.bootstrap5.components.BSBtnDropdown
import com.fastscala.templates.bootstrap5.utils.{BSBtn, ImmediateInputFields}

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem

trait Table5SelectableRowsWithActions extends Table5SelectableRows {

  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

  def actionsForRows(rows: Set[R]): Seq[BSBtn] = Nil

  def actionsBtnToIncludeInDropdown: BSBtn = BSBtn.BtnPrimary.lbl("Actions")

  override def onSelectedRowsChange()(implicit fsc: FSContext): Js = super.onSelectedRowsChange() &
    actionsDropdownBtnRenderer.rerender()


  lazy val actionsDropdownBtnRenderer: Rerenderer = Js.rerenderable(rerenderer => implicit fsc => {
    BSBtnDropdown(actionsBtnToIncludeInDropdown)(
      actionsForRows(selectedVisibleRows): _*
    )
  }, debugLabel = Some("actions_dropdown_btn"))
}
