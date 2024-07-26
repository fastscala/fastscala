package com.fastscala.templates.bootstrap5.tables

import com.fastscala.core.{FSXmlEnv, FSXmlSupport}
import com.fastscala.js.rerenderers.Rerenderer


class AroundId(val id: String) extends AnyVal

class TableId(val id: String) extends AnyVal

class TableRowIdx(val idx: Int) extends AnyVal

class TableColIdx(val idx: Int) extends AnyVal

case class AroundRerenderer[E <: FSXmlEnv : FSXmlSupport](rerenderer: Rerenderer[E])

case class TableRerenderer[E <: FSXmlEnv : FSXmlSupport](rerenderer: Rerenderer[E])

case class TableHeadRerenderer[E <: FSXmlEnv : FSXmlSupport](rerenderer: Rerenderer[E])

case class TableBodyRerenderer[E <: FSXmlEnv : FSXmlSupport](rerenderer: Rerenderer[E])

case class THRerenderer[E <: FSXmlEnv : FSXmlSupport](rerenderer: Rerenderer[E])

case class TRRerenderer[E <: FSXmlEnv : FSXmlSupport](rerenderer: Rerenderer[E])

case class TDRerenderer[E <: FSXmlEnv : FSXmlSupport](rerenderer: Rerenderer[E])
