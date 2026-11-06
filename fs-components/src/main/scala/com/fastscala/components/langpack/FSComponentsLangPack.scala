package com.fastscala.components.langpack

object FSComponentsLangPack {
  object Default {
    implicit val EN: FSComponentsLangPackEN = new FSComponentsLangPackEN {}
  }
}

trait FSComponentsLangPack {

  def tableSelectColumns: String

  def tableDone: String

  def tableColumns: String

  def tableClearSelection: String

  def tableSelectAll: String

  def tableActions: String

  def tableSizeInfo(from: Int, to: Int, pageSize: Int, total: Option[Int]): Option[String]

  def modalClose: String

  def modalSave: String

  def modalCancel: String

  def modalDone: String
}
