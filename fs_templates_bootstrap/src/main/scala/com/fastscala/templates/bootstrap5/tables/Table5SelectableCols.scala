package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.bootstrap5.modals.{BSModal5, BSModal5Base}
import com.fastscala.templates.bootstrap5.utils.{BSBtn, ImmediateInputFields}
import com.fastscala.utils.Lazy
import com.fastscala.utils.NodeSeqUtils.MkNSFromNodeSeq

import scala.xml.Elem

trait Table5SelectableCols extends Table5Base with Table5ColsLabeled {

  lazy val currentSelectedCols: Lazy[collection.mutable.Set[C]] = Lazy(collection.mutable.Set(allColumns().filter(columnStartsVisible): _*))

  def allColumns(): Seq[C]

  def columnStartsVisible(c: C): Boolean = true

  def columns(): Seq[C] = allColumns().filter(col => currentSelectedCols().contains(col))

  def colLabel(col: C): String

  def renderSelectColsButton()(implicit fsc: FSContext): Elem =
    BSBtn.BtnPrimary.lbl("Columns").ajax(implicit fsc => openColumnSelectionModal()).btn

  def openColumnSelectionModal()(implicit fsc: FSContext): Js = BSModal5.verySimple(
    "Select Columns",
    "Done",
    onHidden = fsc.callback(() => rerenderTableAround())
  )(modal => implicit fsc => {
    allColumns().map(col => {
      ImmediateInputFields.checkbox(
        () => currentSelectedCols().contains(col),
        {
          case true =>
            currentSelectedCols() += col
            Js.void
          case false =>
            currentSelectedCols() -= col
            Js.void
        },
        colLabel(col)
      )
    }).mkNS
  })
}
