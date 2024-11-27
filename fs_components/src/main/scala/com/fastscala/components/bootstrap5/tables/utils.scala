package com.fastscala.components.bootstrap5.tables

import com.fastscala.scala_xml.rerenderers.Rerenderer


class AroundId(val id: String) extends AnyVal

class TableId(val id: String) extends AnyVal

class TableRowIdx(val idx: Int) extends AnyVal

class TableColIdx(val idx: Int) extends AnyVal

case class AroundRerenderer(rerenderer: Rerenderer)

case class TableRerenderer(rerenderer: Rerenderer)

case class TableHeadRerenderer(rerenderer: Rerenderer)

case class TableBodyRerenderer(rerenderer: Rerenderer)

case class THRerenderer(rerenderer: Rerenderer)

case class TRRerenderer(rerenderer: Rerenderer)

case class TDRerenderer(rerenderer: Rerenderer)
