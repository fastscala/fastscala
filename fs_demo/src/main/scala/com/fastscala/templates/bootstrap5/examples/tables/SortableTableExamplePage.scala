package com.fastscala.templates.bootstrap5.examples.tables

import com.fastscala.core.FSContext
import com.fastscala.templates.bootstrap5.examples.SingleCodeExamplePage
import com.fastscala.templates.bootstrap5.examples.data.{CountriesData, Country}
import com.fastscala.templates.bootstrap5.tables._

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.NodeSeq


class SortableTableExamplePage extends SingleCodeExamplePage("/com/fastscala/templates/bootstrap5/examples/tables/SortableTableExamplePage.scala") {

  override def pageTitle: String = "Sortable Table Example"

  // === code snippet ===
  override def renderExampleContents()(implicit fsc: FSContext): NodeSeq = {
    new Table5Base
      with Table5BaseBootrapSupport
      with Table5StandardColumns
      with Table5SeqDataSource
      with Table5Sortable {
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

      override def rowsSorter: PartialFunction[Table5StandardColumn[Country], Seq[Country] => Seq[Country]] = {
        case ColName => _.sortBy(_.name.common)
        case ColCapital => _.sortBy(_.capital.mkString(", "))
        case ColRegion => _.sortBy(_.region)
        case ColArea => _.sortBy(_.area)
      }

      override def seqRowsSource(): Seq[Country] = CountriesData.data
    }.render()
  }
  // === code snippet ===
}
