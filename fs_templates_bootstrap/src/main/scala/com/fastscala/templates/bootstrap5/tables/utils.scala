package com.fastscala.templates.bootstrap5.tables

import com.fastscala.xml.scala_xml.JS.ScalaXmlRerenderer


class AroundId(val id: String) extends AnyVal

class TableId(val id: String) extends AnyVal

class TableRowIdx(val idx: Int) extends AnyVal

class TableColIdx(val idx: Int) extends AnyVal

case class AroundRerenderer(rerenderer: ScalaXmlRerenderer)

case class TableRerenderer(rerenderer: ScalaXmlRerenderer)

case class TableHeadRerenderer(rerenderer: ScalaXmlRerenderer)

case class TableBodyRerenderer(rerenderer: ScalaXmlRerenderer)

case class THRerenderer(rerenderer: ScalaXmlRerenderer)

case class TRRerenderer(rerenderer: ScalaXmlRerenderer)

case class TDRerenderer(rerenderer: ScalaXmlRerenderer)
