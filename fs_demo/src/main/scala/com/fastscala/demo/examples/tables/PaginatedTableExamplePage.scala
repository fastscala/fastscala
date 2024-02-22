package com.fastscala.demo.examples.tables

import com.fastscala.core.FSContext
import com.fastscala.demo.examples.SingleCodeExamplePage
import com.fastscala.demo.examples.data.{CountriesData, Country}
import com.fastscala.templates.bootstrap5.tables._

import scala.xml.NodeSeq


class PaginatedTableExamplePage extends SingleCodeExamplePage("/com/fastscala/demo/examples/tables/PaginatedTableExamplePage.scala") {

  override def pageTitle: String = "Paginated Table Example"

  // === code snippet ===
  override def renderExampleContents()(implicit fsc: FSContext): NodeSeq = {
    new Table5Base
      with Table5BaseBootrapSupport
      with Table5StandardColumns
      with Table5SeqSortableDataSource
      with Table5Paginated {

      override type R = Country



      override def defaultPageSize = 10

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


      override def rowsSorter: PartialFunction[Table5StandardColumn[Country], Seq[Country] => Seq[Country]] = {
        case ColName => _.sortBy(_.name.common)
      }

      override def seqRowsSource: Seq[Country] = CountriesData.data
    }.render()
  }
  // === code snippet ===
}
