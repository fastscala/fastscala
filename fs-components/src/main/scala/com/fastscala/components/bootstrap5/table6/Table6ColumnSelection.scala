package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.bootstrap5.components.BSBtnDropdown
import com.fastscala.components.bootstrap5.helpers.BSHelpers.RichAttributeEnrichable
import com.fastscala.components.bootstrap5.modals.BSModal5
import com.fastscala.components.bootstrap5.utils.{BSBtn, ImmediateInputFields}
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromElems
import com.fastscala.scala_xml.js.JS
import com.fastscala.utils.Lazy

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem

trait Table6ColumnSelection extends Table6Base with Table6ColsLabeled {

  var onDropdownBtnTransforms: Elem => Elem = identity[Elem]

  def dropdownBtnClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def dropdownBtnStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def onDropdownBtnWrapper(f: Elem => Elem): this.type = mutate {
    onDropdownBtnTransforms = onDropdownBtnTransforms.pipe(onDropdownBtnTransforms => elem => f(onDropdownBtnTransforms(elem)))
  }

  var onDropdownIndividualBtnTransforms: Elem => Elem = identity[Elem]

  def dropdownIndividualBtnClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def dropdownIndividualBtnStyle()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String = ""

  def onDropdownIndividualBtnWrapper(f: Elem => Elem): this.type = mutate {
    onDropdownIndividualBtnTransforms = onDropdownIndividualBtnTransforms.pipe(onDropdownIndividualBtnTransforms => elem => f(onDropdownIndividualBtnTransforms(elem)))
  }

  lazy val currentSelectedCols: Lazy[collection.mutable.Set[C]] = Lazy(collection.mutable.Set(allColumns.filter(columnStartsVisible)*))

  def allColumns: Seq[C]

  val initiallyVisibleColumns: Set[C] 
  
  def columnStartsVisible(c: C): Boolean = initiallyVisibleColumns.contains(c)

  def columns(): Seq[C] = {
    if (currentSelectedCols().exists(!allColumns.contains(_))) throw new Exception(
      "Column is selected but does not exist in allColumns: is allColumns a method and you're creating always new columns which are not equal (.equals(obj), ==) to the old ones? Maybe use a lazy val allColumns instead of a def allColumns."
    )
    allColumns.filter(col => currentSelectedCols().contains(col))
  }

  def colLabel(col: C): String

  def renderColumnSelectionButton()(implicit fsc: FSContext): Elem =
    BSBtn().BtnPrimary.lbl("Columns").ajax(implicit fsc => openColumnSelectionModal()).btn

  def openColumnSelectionModal()(implicit fsc: FSContext): Js = BSModal5.verySimple(
    "Select Columns",
    "Done",
    onHidden = fsc.callback(() => rerender())
  )(modal =>
    implicit fsc => {
      import com.fastscala.components.bootstrap5.helpers.BSHelpers.*
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
            () => currentSelectedCols().contains(col),
            {
              case true =>
                currentSelectedCols() += col
                rerender()
              case false =>
                currentSelectedCols() -= col
                rerender()
            },
            colLabel(col)
          )).withStyle("padding-top: 1px; padding-bottom: 1px;").btnLink
        )
      )*
    ))
}
