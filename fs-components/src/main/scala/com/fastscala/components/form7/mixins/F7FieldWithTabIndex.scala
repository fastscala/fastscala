package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithTabIndex extends F7FieldInputFieldMixin with Mutable {
  var _tabIndex: F7FieldMixinStatus[Option[Int]] = F7FieldMixinStatus(None)

  def tabIndex: Option[Int] = _tabIndex()

  def tabIndex(v: Option[Int]): this.type = mutate {
    _tabIndex() = () => v
  }

  def tabIndex(v: Int): this.type = mutate {
    _tabIndex() = () => Some(v)
  }

  def tabIndex(f: () => Option[Int]): this.type = mutate {
    _tabIndex() = f
  }

  override def preRender(): Unit = {
    super.preRender()
    _tabIndex.setRendered()
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _tabIndex().map(tabIndex => input.withAttr("tabindex", tabIndex.toString)).getOrElse(input)
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ & _tabIndex.updateIfChanged({
      case (_, Some(tabindex)) => JS.setAttr(elemId)("tabindex", tabindex.toString)
      case (_, None) => JS.removeAttr(elemId, "tabindex")
    }, Js.Void))
}
