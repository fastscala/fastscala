package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.bootstrap5.components.BSBtnDropdown
import com.fastscala.components.bootstrap5.helpers.BSHelpers.RichAttributeEnrichable
import com.fastscala.components.bootstrap5.modals.BSModal5
import com.fastscala.components.bootstrap5.utils.{BSBtn, ImmediateInputFields}
import com.fastscala.core.{FSContext, FSPage}
import com.fastscala.js.Js
import com.fastscala.scala_xml.xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.xml.ScalaXmlNodeSeqUtils.MkNSFromElems
import com.fastscala.scala_xml.js.JS
import com.fastscala.utils.{Lazy, LazyWithPage}

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem

trait Table6ColumnSelection extends Table6Base with Table6ColsLabeled {

  var onDropdownBtnTransforms: Elem => Elem = identity[Elem]

  def dropdownBtnClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)], knownTotalNumberOfRows: KnownTotalNumberOfRowsMaterialized): String = ""

  def dropdownBtnStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)], knownTotalNumberOfRows: KnownTotalNumberOfRowsMaterialized): String = ""

  def onDropdownBtnWrapper(f: Elem => Elem): this.type = mutate {
    onDropdownBtnTransforms = onDropdownBtnTransforms.pipe(onDropdownBtnTransforms => elem => f(onDropdownBtnTransforms(elem)))
  }

  var onDropdownIndividualBtnTransforms: Elem => Elem = identity[Elem]

  def dropdownIndividualBtnClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)], knownTotalNumberOfRows: KnownTotalNumberOfRowsMaterialized): String = ""

  def dropdownIndividualBtnStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)], knownTotalNumberOfRows: KnownTotalNumberOfRowsMaterialized): String = ""

  def onDropdownIndividualBtnWrapper(f: Elem => Elem): this.type = mutate {
    onDropdownIndividualBtnTransforms = onDropdownIndividualBtnTransforms.pipe(onDropdownIndividualBtnTransforms => elem => f(onDropdownIndividualBtnTransforms(elem)))
  }

  lazy val currentSelectedCols: LazyWithPage[collection.mutable.Set[C]] = LazyWithPage(implicit page => collection.mutable.Set(allColumns.filter(columnStartsVisible) *))

  def allColumns: Seq[C]

  lazy val initiallyVisibleColumns: Set[C] = allColumns.toSet

  def columnStartsVisible(c: C)(implicit fsc: FSPage): Boolean = initiallyVisibleColumns.contains(c)

  def columns()(implicit fsc: FSPage): Seq[C] = allColumns

  override def visibleColumns()(implicit fsc: FSContext): Seq[C] = {
    if (currentSelectedCols()(using fsc.page).exists(!allColumns.contains(_))) throw new Exception(
      "Column is selected but does not exist in allColumns: is allColumns a method and you're creating always new columns which are not equal (.equals(obj), ==) to the old ones? " +
        "Maybe use a lazy val allColumns instead of a def allColumns."
    )
    allColumns.filter(col => currentSelectedCols()(using fsc.page).contains(col))
  }

  def colLabel(col: C): String

  def renderColumnSelectionButton()(implicit fsc: FSContext): Elem =
    BSBtn().BtnPrimary.lbl(langPack.tableColumns).callback(implicit fsc => openColumnSelectionModal()).btn

  def onColumnSelectionChange()(implicit fsc: FSContext): Js = Js.Void

  def openColumnSelectionModal()(implicit fsc: FSContext): Js = BSModal5.verySimple(
    langPack.tableSelectColumns,
    langPack.modalDone,
    onHidden = fsc.callback(() => onColumnSelectionChange() & rerender())
  )(modal =>
    implicit fsc => {
      import com.fastscala.components.bootstrap5.helpers.BSHelpers.*
      allColumns.map(col => {
        ImmediateInputFields.checkbox(
          () => currentSelectedCols()(using fsc.page).contains(col),
          {
            case true =>
              currentSelectedCols()(using fsc.page) += col
              JS.void
            case false =>
              currentSelectedCols()(using fsc.page) -= col
              JS.void
          },
          colLabel(col)
        ).mb_2
      }).mkNS
    }
  )

  def columnSelectionDropdownBtnRightAligned: Boolean = true

  def columnSelectionDropdownBtn()(implicit fsc: FSContext): BSBtn = BSBtn().BtnPrimary.lbl("Columns...").sm.dataBsAutoCloseAsOutside

  def columnSelectionDropdownIndividualBtn()(implicit fsc: FSContext): BSBtn = BSBtn()

  def columnSelectionDropdown()(implicit fsc: FSContext): Elem =
    onDropdownBtnTransforms(BSBtnDropdown.custom(columnSelectionDropdownBtn(), rightAlignedMenu = columnSelectionDropdownBtnRightAligned)(
      allColumns.map(col =>
        onDropdownIndividualBtnTransforms(
          columnSelectionDropdownIndividualBtn().ns(ImmediateInputFields.checkbox(
            () => currentSelectedCols()(using fsc.page).contains(col),
            {
              case true =>
                currentSelectedCols()(using fsc.page) += col
                onColumnSelectionChange() & rerender()
              case false =>
                currentSelectedCols()(using fsc.page) -= col
                onColumnSelectionChange() & rerender()
            },
            colLabel(col)
          )).withStyle("padding-top: 1px; padding-bottom: 1px;").btnLink
        )
      ) *
    ))
}
