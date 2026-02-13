package com.fastscala.components.bootstrap5.table6

import com.fastscala.core.FSContext

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem

object Table6BootrapStyles extends Enumeration {
  val Primary = Value("table-primary")
  val Secondary = Value("table-secondary")
  val Success = Value("table-success")
  val Info = Value("table-info")
  val Warning = Value("table-warning")
  val Danger = Value("table-danger")
  val Light = Value("table-light")
  val Dark = Value("table-dark")
}

object Table6BootrapBorderStyles extends Enumeration {
  val Primary = Value("table-primary")
  val Secondary = Value("table-secondary")
  val Success = Value("table-success")
  val Info = Value("table-info")
  val Warning = Value("table-warning")
  val Danger = Value("table-danger")
  val Light = Value("table-light")
  val Dark = Value("table-dark")
}

object Table6BootrapResponsiveSizes extends Enumeration {
  val ALL = Value("table-responsive")
  val SM = Value("table-responsive-sm")
  val MD = Value("table-responsive-md")
  val LG = Value("table-responsive-lg")
  val XL = Value("table-responsive-xl")
  val XXL = Value("table-responsive-xxl")
}

trait Table6BootrapStyling extends Table6Base {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  def tableResponsive: Option[Table6BootrapResponsiveSizes.Value] = None

  def tableDark: Boolean = false

  def tableSmall: Boolean = false

  def tableBorderless: Boolean = false

  def tableBordered: Boolean = false

  def tableHoverable: Boolean = false

  def tableStriped: Boolean = false

  def tableStripedColumns: Boolean = false

  def tableStyle: Option[Table6BootrapStyles.Value] = None

  def tableHeadStyle: Option[Table6BootrapStyles.Value] = None

  def tableBorderStyle: Option[Table6BootrapBorderStyles.Value] = None

  override def tableClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)], knownTotalNumberOfRows: Option[Int]): String =
    super.tableClasses() + " table " +
      (if (tableDark) " table-dark " else "") +
      (if (tableStriped) " table-striped " else "") +
      (if (tableStripedColumns) " table-striped-columns " else "") +
      (if (tableHoverable) " table-hover " else "") +
      (if (tableBordered) " table-bordered " else "") +
      (if (tableBorderless) " table-borderless " else "") +
      (if (tableSmall) " table-sm " else "")

  override def tableHeadClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)], knownTotalNumberOfRows: Option[Int]): String =
    super.tableHeadClasses() +
      tableHeadStyle.map(" " + _ + " ").getOrElse("")

  override def transformTableElem(elem: Elem)(implicit fsc: FSContext, columns: Seq[(String, C)], rows: Seq[(String, R)], knownTotalNumberOfRows: Option[Int]): Elem =
    super.transformTableElem(elem).pipe(elem => tableResponsive.map(size => elem.addClass(size.toString)).getOrElse(elem))
}
