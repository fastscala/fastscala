package com.fastscala.components.langpack

trait FSComponentsLangPackEN extends FSComponentsLangPack {

  def tableSelectColumns = "Select columns"

  def tableDone = "Done"

  def tableColumns = "Columns"

  def tableClearSelection = "Clear Selection"

  def tableSelectAll = "Select All"

  def tableActions = "Actions"

  def tableSizeInfo(from: Int, to: Int, pageSize: Int, total: Option[Int]): Option[String] = total.map(total => s"Showing $pageSize of $total")

  def modalClose = "Close"

  def modalSave = "Save"

  def modalCancel = "Cancel"

  def modalDone = "Done"
}
