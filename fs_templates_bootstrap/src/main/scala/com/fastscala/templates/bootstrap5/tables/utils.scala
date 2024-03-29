package com.fastscala.templates.bootstrap5.tables

import com.fastscala.js.Rerenderer


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
