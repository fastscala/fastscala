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


trait F7FieldWithSize extends F7FieldWithMainElem with Mutable {
  private val _size: F7FieldMixinStatus[Option[Int]] = F7FieldMixinStatus(None)

  def size(): Option[Int] = _size()

  def size(v: Int): this.type = mutate {
    _size() = () => Some(v)
  }

  def size(v: Option[Int]): this.type = mutate {
    _size() = () => v
  }

  def size(f: () => Option[Int]): this.type = mutate {
    _size() = f
  }

  override def processMainElem(input: Elem): Elem = super.processMainElem(input).pipe { input =>
    _size().map(size => input.withAttr("size", size.toString)).getOrElse(input)
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ & _size.updateIfChanged({
      case (_, Some(size)) => JS.setAttr(mainElemId)("size", size.toString)
      case (_, None) => JS.removeAttr(mainElemId, "size")
    }, Js.Void))
}
