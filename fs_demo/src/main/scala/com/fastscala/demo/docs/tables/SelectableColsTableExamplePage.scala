package com.fastscala.demo.docs.tables

import com.fastscala.core.FSContext
import com.fastscala.demo.docs.MultipleCodeExamples2Page
import com.fastscala.demo.docs.components.Widget
import com.fastscala.demo.docs.data.{CountriesData, Country}
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.templates.bootstrap5.tables.*
import com.fastscala.templates.bootstrap5.utils.BSBtn
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import java.util.Date
import scala.xml.{Elem, NodeSeq}


class SelectableColsTableExamplePage extends MultipleCodeExamples2Page() {

  override def pageTitle: String = "Selectable cols table example"

  override def renderContentsWithSnippets()(implicit fsc: FSContext): Unit = {
    renderSnippet("Source") {
      import com.fastscala.templates.bootstrap5.helpers.BSHelpers.*

      lazy val mainTable = new Table5Base
        with Table5BaseBootrapSupport
        with Table5Paginated
        with Table5SeqSortableDataSource
        with Table5StandardColumns
        with Table5Sortable
        with Table5SelectableCols {
        override type R = Country

        override def defaultPageSize: Int = 10

        override def aroundClasses()(implicit fsc: FSContext): String = super.aroundClasses() + " mb-5"

        override def tableHeadStyle: Option[Table5BootrapStyles.Value] = Some(Table5BootrapStyles.Primary)

        override def tableResponsive: Option[Table5BootrapResponsiveSizes.Value] = Some(Table5BootrapResponsiveSizes.ALL)

        val ColActions = ColNs("Actions", implicit fsc => row => BSBtn().BtnPrimary.sm.lbl("Time?").ajax(implicit fsc => {
          JS.alert(s"Time on server is: ${new Date().toGMTString}")
        }).btn)
        val ColName = ColStr("Name", _.name.common)
        val ColCCA2 = ColStr("CCA2", _.cca2)
        val ColCCN3 = ColStr("CCN3", _.ccn3)
        val ColCCA3 = ColStr("CCA3", _.cca3)
        val ColCIOC = ColStr("CIOC", _.cioc)
        val ColStatus = ColStr("Status", _.status)
        val ColUNMember = ColStr("UN Member", _.unMember.map(_.toString).getOrElse("--"))
        val ColCapital = ColStr("Capital", _.capital.mkString(", "))
        val ColAltSpellings = ColStr("Alt Spellings", _.altSpellings.mkString(", "))
        val ColRegion = ColStr("Region", _.region)
        val ColSubregion = ColStr("Subregion", _.subregion)
        val ColLatLng = ColStr("LatLng", _.latlng.map(_.mkString(";")).getOrElse("--"))
        val ColLandlocked = ColStr("Landlocked", _.landlocked.map(_.toString).getOrElse("--"))
        val ColBorders = ColStr("Borders", _.borders.mkString(", "))
        val ColArea = ColStr("Area", _.area.toString)
        val ColCallingCodes = ColStr("Calling Codes", _.callingCodes.mkString(", "))
        val ColFlag = ColStr("Flag", _.flag)


        override def rowsSorter: PartialFunction[Table5StandardColumn[Country], Seq[Country] => Seq[Country]] = {
          case ColName => _.sortBy(_.name.common)
          case ColCCA2 => _.sortBy(_.cca2)
          case ColCCN3 => _.sortBy(_.ccn3)
          case ColCCA3 => _.sortBy(_.cca3)
          case ColCIOC => _.sortBy(_.cioc)
          case ColStatus => _.sortBy(_.status)
          case ColUNMember => _.sortBy(_.unMember.map(_.toString).getOrElse("--"))
          case ColCapital => _.sortBy(_.capital.mkString(", "))
          case ColAltSpellings => _.sortBy(_.altSpellings.mkString(", "))
          case ColRegion => _.sortBy(_.region)
          case ColSubregion => _.sortBy(_.subregion)
          case ColLatLng => _.sortBy(_.latlng.map(_.mkString(";")).getOrElse("--"))
          case ColLandlocked => _.sortBy(_.landlocked.map(_.toString).getOrElse("--"))
          case ColBorders => _.sortBy(_.borders.mkString(", "))
          case ColArea => _.sortBy(_.area.toString)
          case ColCallingCodes => _.sortBy(_.callingCodes.mkString(", "))
          case ColFlag => _.sortBy(_.flag)
        }

        override def allColumns: List[C] = List(
          ColName
          , ColCCA2
          , ColCCN3
          , ColCCA3
          , ColCIOC
          , ColStatus
          , ColUNMember
          , ColCapital
          , ColAltSpellings
          , ColRegion
          , ColSubregion
          , ColLatLng
          , ColLandlocked
          , ColBorders
          , ColArea
          , ColCallingCodes
          , ColFlag
          , ColActions
        )

        override def columnStartsVisible(c: Table5StandardColumn[Country]): Boolean = Set(
          ColName
          , ColUNMember
          , ColCapital
          , ColArea
          , ColActions
        ).contains(c)

        override def seqRowsSource: Seq[Country] = CountriesData.data
      }

      new Widget {
        override def widgetTitle: String = "Selectable cols"

        override def transformWidgetCardBody(elem: Elem): Elem = super.transformWidgetCardBody(elem).p_0

        override def widgetTopRight()(implicit fsc: FSContext): NodeSeq = mainTable.colSelectionDropdownBtn(_.BtnPrimary.lbl("Columns...").sm.dataBsAutoCloseAsOutside)

        override def widgetContents()(implicit fsc: FSContext): NodeSeq = mainTable.render()
      }.renderWidget()
    }
    closeSnippet()
  }
}
