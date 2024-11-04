package com.fastscala.demo.docs.fastscala

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.demo.docs.components.Widget
import com.fastscala.demo.docs.data.{CountriesData, Country}
import com.fastscala.js.rerenderers.RerendererDebugStatus
import com.fastscala.templates.bootstrap5.tables._

import scala.xml.{Elem, NodeSeq}

class FSContextDiscardPage extends MultipleCodeExamples2Page() {
  override def pageTitle: String = "FSContext discard"

  import com.fastscala.templates.bootstrap5.helpers.BSHelpers._

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {

    renderSnippet("Old FSContext is discarded on rerender, so the number of callbacks doesn't grow indefinitely") {

      // Debug rerenderable:
      fsc.page.rerendererDebugStatus = RerendererDebugStatus.Enabled

      val table = new Table5Base
        with Table5BaseBootrapSupport
        with Table5SelectableRows
        with Table5StandardColumns
        with Table5Paginated {
        override type R = Country

        override def defaultPageSize: Int = 10

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

        override def seqRowsSource: Seq[Country] = CountriesData.data
      }

      new Widget {
        override def widgetTitle: String = "Selectable rows"

        override def transformWidgetCardBody(elem: Elem): Elem = super.transformWidgetCardBody(elem).p_0

        override def widgetTopRight()(implicit fsc: FSContext): NodeSeq = table.clearRowSelectionBtn.btn ++ table.selectAllVisibleRowsBtn.btn.ms_2

        override def widgetContents()(implicit fsc: FSContext): NodeSeq = table.render()
      }.renderWidget()
    }
    closeSnippet()
  }
}
