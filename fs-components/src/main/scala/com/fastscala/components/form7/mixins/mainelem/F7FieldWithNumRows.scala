package com.fastscala.components.form7.mixins.mainelem

import com.fastscala.components.form7.mixins.mainelem.F7FieldWithMainElem
import com.fastscala.components.form7.{F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithNumRows extends F7FieldWithMainElem with Mutable {
  def defaultNumRows: Option[Int] = None

  private val _numRows: F7FieldMixinStatus[Option[Int]] = F7FieldMixinStatus(defaultNumRows)

  def numRows: Option[Int] = _numRows()

  def numRows(v: Option[Int]): this.type = mutate {
    _numRows() = () => v
  }

  def numRows(v: Int): this.type = mutate {
    _numRows() = () => Some(v)
  }

  def numRows(f: () => Option[Int]): this.type = mutate {
    _numRows() = f
  }

  override def processMainElem(input: Elem): Elem = super.processMainElem(input).pipe { input =>
    _numRows().map(rows => input.withAttr("rows", rows.toString)).getOrElse(input)
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ & _numRows.updateIfChanged({
      case (old, None) => JS.removeAttr(mainElemId, "rows")
      case (old, Some(value)) => JS.setAttr(mainElemId)("rows", value.toString)
    }, Js.Void))
}
