package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithAdditionalAttrs extends F7FieldInputFieldMixin with Mutable {
  private val _additionalAttrs: F7FieldMixinStatus[Seq[(String, String)]] = F7FieldMixinStatus(Nil)

  def additionalAttrs: Seq[(String, String)] = _additionalAttrs()

  def additionalAttrs(v: Seq[(String, String)]): this.type = mutate {
    _additionalAttrs() = () => v
  }

  def additionalAttrs(f: () => Seq[(String, String)]): this.type = mutate {
    _additionalAttrs() = f
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    input.withAttrs(_additionalAttrs() *)
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ & _additionalAttrs.updateIfChanged({
      case (old, cur) =>
        val removed = old.map(_._1).toSet -- cur.map(_._1).toSet
        (removed.map(name => JS.removeAttr(elemId, name)).toList :::
          cur.map({ case (attr, value) => JS.setAttr(elemId)(attr, value) }).toList)
          .reduceOption[Js](_ & _).getOrElse(Js.Void)
    }, Js.Void))
}
