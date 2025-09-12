package com.fastscala.components.bootstrap5.table6

import com.fastscala.scala_xml.rerenderers.{Rerenderer, RerendererP}

class TableWrapperId(val id: String) extends AnyVal

class TableId(val id: String) extends AnyVal

class TableRowId(val id: String) extends AnyVal

class TableRowIdx(val idx: Int) extends AnyVal

class TableColId(val id: String) extends AnyVal

class TableColIdx(val idx: Int) extends AnyVal

case class TableWrapperRerenderer(rerenderer: Rerenderer)

case class TableRerenderer(rerenderer: Rerenderer)

case class TableHeadRerenderer(rerenderer: RerendererP[(TableHeadRerenderer, TableBodyRerenderer, TableFootRerenderer)])

case class TableBodyRerenderer(rerenderer: RerendererP[(TableHeadRerenderer, TableBodyRerenderer, TableFootRerenderer)])

case class TableFootRerenderer(rerenderer: RerendererP[(TableHeadRerenderer, TableBodyRerenderer, TableFootRerenderer)])

case class ThRerenderer(rerenderer: Rerenderer)

case class TrRerenderer(rerenderer: Rerenderer)

case class TdRerenderer(rerenderer: Rerenderer)
