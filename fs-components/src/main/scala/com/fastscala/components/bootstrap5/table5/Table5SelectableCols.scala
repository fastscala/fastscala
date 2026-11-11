package com.fastscala.components.bootstrap5.table5

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.components.bootstrap5.components.BSBtnDropdown
import com.fastscala.components.bootstrap5.modals.BSModal5
import com.fastscala.components.bootstrap5.utils.{BSBtn, ImmediateInputFields}
import com.fastscala.utils.Lazy
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromElems

import scala.xml.Elem

trait Table5SelectableCols extends Table5Base with Table5ColsLabeled {

  lazy val currentSelectedCols: Lazy[collection.mutable.Set[C]] = Lazy(collection.mutable.Set(allColumns.filter(columnStartsVisible)*))

  def allColumns: Seq[C]

  def columnStartsVisible(c: C): Boolean = true

  def columns(): Seq[C] = {
    if (currentSelectedCols().exists(!allColumns.contains(_))) throw new Exception("Column is selected but does not exist in allColumns: is allColumns a method and you're creating always new columns which are not equal (.equals(obj), ==) to the old ones? Maybe use a lazy val allColumns instead of a def allColumns.")
    allColumns.filter(col => currentSelectedCols().contains(col))
  }

  def colLabel(col: C): String

  def renderSelectColsButton()(implicit fsc: FSContext): Elem =
    BSBtn().BtnPrimary.lbl("Columns").callback(implicit fsc => openColumnSelectionModal()).btn

  def openColumnSelectionModal()(implicit fsc: FSContext): Js = BSModal5.verySimple(
    "Select Columns",
    "Done",
    onHidden = fsc.callback(() => rerenderTableAround())
  )(modal => implicit fsc => {
    allColumns.map(col => {
      ImmediateInputFields.checkbox(
        () => currentSelectedCols().contains(col),
        {
          case true =>
            currentSelectedCols() += col
            JS.void
          case false =>
            currentSelectedCols() -= col
            JS.void
        },
        colLabel(col)
      )
    }).mkNS
  })

  def colSelectionDropdownBaseBtn()(implicit fsc: FSContext): BSBtn = BSBtn()

  def colSelectionDropdownBtn(
                               transformBaseBtn: BSBtn => BSBtn = identity[BSBtn],
                             )(implicit fsc: FSContext): Elem = BSBtnDropdown(
    transformBaseBtn(colSelectionDropdownBaseBtn())
  )(
    allColumns.map(col => BSBtn().ns(ImmediateInputFields.checkbox(
      () => currentSelectedCols().contains(col),
      {
        case true =>
          currentSelectedCols() += col
          rerenderTable()
        case false =>
          currentSelectedCols() -= col
          rerenderTable()
      },
      colLabel(col)
    )).withStyle("padding-top: 1px; padding-bottom: 1px;"))*
  )
}
