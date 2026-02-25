package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7Field, F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS
import com.fastscala.utils.IdGen

import scala.util.Success
import scala.xml.Elem


trait F7FieldWithLabel extends F7Field with Mutable {
  lazy val _defaultLabelId = "label_" + IdGen.id

  var _labelId: () => String = () => _defaultLabelId

  def labelId: String = _labelId()

  def labelId(v: String): this.type = mutate {
    _labelId = () => v
  }

  private val _label: F7FieldMixinStatus[Option[Elem]] = F7FieldMixinStatus(None)

  def label: Option[Elem] = _label().map(_.withId(labelId))

  def label(v: Option[Elem]): this.type = mutate {
    _label() = () => v
  }

  def labelElemOptF(v: () => Option[Elem]): this.type = mutate {
    _label() = () => v()
  }

  def label(v: Elem): this.type = mutate {
    _label() = () => Some(v)
  }

  def labelElemF(v: () => Elem): this.type = mutate {
    _label() = () => Some(v())
  }

  def label(v: String): this.type = mutate {
    _label() = () => Some(<label>{v}</label>)
  }

  def labelStrF(f: () => String): this.type = mutate {
    _label() = () => Some(<label>{f()}</label>)
  }

  override def toString: String = label.map(_.toString).getOrElse(super.toString)

  override def preRender(): Unit = {
    super.preRender()
    _label.setRendered()
  }

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*
  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().flatMap(js => _label.updateIfChanged({
      case (None, _) => scala.util.Failure(new Exception("Need to rerender"))
      case (Some(_), None) => scala.util.Success(js & JS.removeId(labelId))
      case (Some(_), Some(elem)) => scala.util.Success(js & JS.replace(labelId, elem.withId(labelId)))
    }, Success(Js.Void)))
}