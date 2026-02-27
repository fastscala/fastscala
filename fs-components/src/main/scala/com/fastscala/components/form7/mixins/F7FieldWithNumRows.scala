package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithNumRows extends F7FieldInputFieldMixin with Mutable {
  private val _rows: F7FieldMixinStatus[Option[Int]] = F7FieldMixinStatus(None)

  def rows: Option[Int] = _rows()

  def rows(v: Option[Int]): this.type = mutate {
    _rows() = () => v
  }

  def rows(v: Int): this.type = mutate {
    _rows() = () => Some(v)
  }

  def rows(f: () => Option[Int]): this.type = mutate {
    _rows() = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _rows().map(rows => input.withAttr("rows", rows.toString)).getOrElse(input)
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ & _rows.updateIfChanged({
      case (old, None) => JS.removeAttr(elemId, "rows")
      case (old, Some(value)) => JS.setAttr(elemId)("rows", value.toString)
    }, Js.Void))
}
