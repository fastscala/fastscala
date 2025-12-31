package com.fastscala.components.bootstrap5.table6

import com.fastscala.components.bootstrap5.utils.{BSBtn, ImmediateInputFields}
import com.fastscala.core.FSContext
import com.fastscala.js.{Js, JsFunc1}
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.{JS, printBeforeExec}

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem

trait Table6SelectableRowsBase extends Table6Base {

  def allSelectedRowsEvenIfNotVisible: collection.mutable.Set[R]

  def selectedVisibleRows: Seq[R]

  def onSelectedRowsChange()(implicit fsc: FSContext): Js = JS.void

  def ColSelectRow: Table6StandardColumn[R]

  def selectAllVisibleRowsBtn: BSBtn

  def clearRowSelectionBtn: BSBtn
}
