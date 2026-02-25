package com.fastscala.components.bootstrap5.table6

trait Table6LangPack {

  def i18n_selectColumns = "Select columns"

  def i18n_done = "Done"

  def i18n_columns = "Columns"

  def i18n_clearSelection = "Clear Selection"

  def i18n_selectAll = "Select All"

  def i18n_actions = "Actions"
  
  def i18n_tableSizeInfo(from: Int, to: Int, pageSize: Int, total: Option[Int]): Option[String] = total.map(total => s"Showing $pageSize of $total")
}
