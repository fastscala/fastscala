package com.fastscala.demo.docs.tables

import com.fastscala.core.FSContext
import com.fastscala.demo.db.User
import com.fastscala.demo.docs.SingleCodeExamplePage
import com.fastscala.demo.docs.data.{CountriesData, Country}
import com.fastscala.templates.bootstrap5.tables._

import scala.xml.NodeSeq


class SimpleTableExamplePage extends SingleCodeExamplePage() {

  override def pageTitle: String = "Table example"

  // === code snippet ===
  override def renderExampleContents()(implicit fsc: FSContext): NodeSeq = {
    new Table5Base
      with Table5BaseBootrapSupport
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
      )

      override def rows(hints: Seq[RowsHint]): Seq[Country] = CountriesData.data
    }.render()
  }

  // === code snippet ===
}
