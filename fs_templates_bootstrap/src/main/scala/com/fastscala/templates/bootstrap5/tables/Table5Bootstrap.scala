package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.FSContext

import scala.xml.Elem

object Table5BootrapStyles extends Enumeration {
  val Primary = Value("table-primary")
  val Secondary = Value("table-secondary")
  val Success = Value("table-success")
  val Info = Value("table-info")
  val Warning = Value("table-warning")
  val Danger = Value("table-danger")
  val Light = Value("table-light")
  val Dark = Value("table-dark")
}

object Table5BootrapBorderStyles extends Enumeration {
  val Primary = Value("table-primary")
  val Secondary = Value("table-secondary")
  val Success = Value("table-success")
  val Info = Value("table-info")
  val Warning = Value("table-warning")
  val Danger = Value("table-danger")
  val Light = Value("table-light")
  val Dark = Value("table-dark")
}

object Table5BootrapResponsiveSizes extends Enumeration {
  val ALL = Value("table-responsive")
  val SM = Value("table-responsive-sm")
  val MD = Value("table-responsive-md")
  val LG = Value("table-responsive-lg")
  val XL = Value("table-responsive-xl")
  val XXL = Value("table-responsive-xxl")
}

trait Table5BaseBootrapSupport extends Table5Base {

  def tableResponsive: Option[Table5BootrapResponsiveSizes.Value] = None

  def tableSmall: Boolean = false

  def tableBorderless: Boolean = false

  def tableBordered: Boolean = false

  def tableHoverable: Boolean = false

  def tableStriped: Boolean = false

  def tableStripedColumns: Boolean = false

  def tableStyle: Option[Table5BootrapStyles.Value] = None

  def tableHeadStyle: Option[Table5BootrapStyles.Value] = None

  def tableBorderStyle: Option[Table5BootrapBorderStyles.Value] = None

  override def tableClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String =
    super.tableClasses() + " table " +
      (if (tableStriped) " table-striped " else "") +
      (if (tableStripedColumns) " table-striped-columns " else "") +
      (if (tableHoverable) " table-hover " else "") +
      (if (tableBordered) " table-bordered " else "") +
      (if (tableBorderless) " table-borderless " else "") +
      (if (tableSmall) " table-sm " else "") +
      tableStyle.map(" " + _ + " ").getOrElse("") +
      tableBorderStyle.map(" " + _ + " ").getOrElse("")

  override def tableHeadClasses()(implicit columns: Seq[(String, C)], rows: Seq[(String, R)]): String =
    super.tableHeadClasses() + " table " +
      tableHeadStyle.map(" " + _ + " ").getOrElse("")

  override def renderTable()(implicit fsc: FSContext): Elem = tableResponsive.map(size => {
    <div class={size.toString}>{super.renderTable()}</div>
  }).getOrElse(super.renderTable())
}
