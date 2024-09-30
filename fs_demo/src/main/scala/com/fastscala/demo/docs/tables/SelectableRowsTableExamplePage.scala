package com.fastscala.demo.docs.tables

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.SingleCodeExamplePage
import com.fastscala.demo.docs.components.Widget
import com.fastscala.demo.docs.data.{CountriesData, Country}
import com.fastscala.templates.bootstrap5.tables._

import scala.xml.{Elem, NodeSeq}


class SelectableRowsTableExamplePage extends SingleCodeExamplePage() {

  override def pageTitle: String = "Selectable rows table example"

  import com.fastscala.templates.bootstrap5.helpers.BSHelpers._

  override def renderExampleContents()(implicit fsc: FSContext): NodeSeq = {
    // === code snippet ===
    val table = new Table5Base
      with Table5BaseBootrapSupport
      with Table5SelectableRows
      with Table5StandardColumns {
      override type R = Country

      val ColName = ColStr("Name", _.name.common)
      val ColCapital = ColStr("Capital", _.capital.mkString(", "))
      val ColRegion = ColStr("Region", _.region)
      val ColArea = ColStr("Area", _.area.toString)

      override def columns(): List[C] = List(
        ColName
        , ColCapital
        , ColRegion
        , ColArea
        , ColSelectRow
      )

      override def rows(hints: Seq[RowsHint]): Seq[Country] = CountriesData.data
    }
    // === code snippet ===

    new Widget {
      override def widgetTitle: String = "Selectable rows"

      override def transformWidgetCardBody(elem: Elem): Elem = super.transformWidgetCardBody(elem).p_0

      override def widgetTopRight()(implicit fsc: FSContext): NodeSeq = table.clearRowSelectionBtn.btn ++ table.selectAllVisibleRowsBtn.btn.ms_2

      override def widgetContents()(implicit fsc: FSContext): NodeSeq = table.render()
    }.renderWidget()
  }
}
