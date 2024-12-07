package com.fastscala.demo.docs.tables

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.demo.docs.data.{CountriesData, Country}
import com.fastscala.components.bootstrap5.tables.*


class SimpleTableExamplePage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Table example"

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderSnippet("Source") {
      new Table5Base
        with Table5BaseBootrapSupport
        with Table5StandardColumns {
        override type R = Country

        override def tableStriped: Boolean = true


        val ColName = ColStr("Name", _.name.common)
        val ColCapital = ColStr("Capital", _.capital.mkString(", "))
        val ColRegion = ColStr("Region", _.region)
        val ColArea = ColStr("Area", _.area.toString)

        override def columns(): List[C] = List(
          ColName
          , ColCapital
          , ColRegion
          , ColArea
        )

        override def rows(hints: Seq[RowsHint]): Seq[Country] = CountriesData.data
      }.render()
    }
    closeSnippet()
  }
}
