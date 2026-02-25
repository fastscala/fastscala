package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.Elem


trait F7FieldWithStep extends F7FieldInputFieldMixin with Mutable {
  var _step: F7FieldMixinStatus[Option[Int]] = F7FieldMixinStatus(None)

  def step(): Option[Int] = _step()

  def step(v: Option[Int]): this.type = mutate {
    _step() = () => v
  }

  def step(v: Int): this.type = mutate {
    _step() = () => Some(v)
  }

  def step(f: () => Option[Int]): this.type = mutate {
    _step() = f
  }

  override def preRender(): Unit = {
    super.preRender()
    _step.setRendered()
  }

  override def processInputElem(input: Elem): Elem = super.processInputElem(input).pipe { input =>
    _step().map(step => input.withAttr("step", step.toString)).getOrElse(input)
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().map(_ & _step.updateIfChanged({
      case (_, Some(step)) => JS.setAttr(elemId)("step", step.toString)
      case (_, None) => JS.removeAttr(elemId, "step")
    }, Js.Void))
}
