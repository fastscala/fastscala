package com.fastscala.templates.bootstrap5.examples.tables

import com.fastscala.code.FSContext
import com.fastscala.templates.bootstrap5.examples.ExampleWithCodePage
import com.fastscala.templates.bootstrap5.examples.components.Widget
import com.fastscala.templates.bootstrap5.examples.data.{CountriesData, Country}
import com.fastscala.templates.bootstrap5.tables._

import scala.xml.{Elem, NodeSeq}


class SelectableRowsTableExamplePage extends ExampleWithCodePage("/com/fastscala/templates/bootstrap5/examples/tables/SelectableRowsTableExamplePage.scala") {

  override def pageTitle: String = "Selectable rows table example"

  import com.fastscala.templates.bootstrap5.classes.BSHelpers._

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

      override def transformCardBody(elem: Elem): Elem = super.transformCardBody(elem).p_0

      override def widgetTopRight()(implicit fsc: FSContext): NodeSeq = table.clearRowSelectionBtn.btn ++ table.selectAllVisibleRowsBtn.btn.ms_2

      override def widgetContents()(implicit fsc: FSContext): NodeSeq = table.render()
    }.render()
  }
}
