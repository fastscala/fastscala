package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.JS
import com.fastscala.utils.IdGen

import scala.util.Success
import scala.xml.Elem


trait F7FieldWithValidFeedback extends F7FieldInputFieldMixin with Mutable {
  lazy val _defaultValidFeedbackId = "valid_feedback_" + IdGen.id

  private var _validFeedbackId: () => String = () => _defaultValidFeedbackId

  def validFeedbackId: String = _validFeedbackId()

  def validFeedbackId(v: String): this.type = mutate {
    _validFeedbackId = () => v
  }

  private val _validFeedback: F7FieldMixinStatus[Option[Elem]] = F7FieldMixinStatus(None)

  def validFeedback: Option[Elem] = _validFeedback().map(_.withId(validFeedbackId))

  def validFeedback(v: Option[Elem]): this.type = mutate {
    _validFeedback() = () => v
  }

  def validFeedback(v: Elem): this.type = mutate {
    _validFeedback() = () => Some(v)
  }

  def validFeedback(v: String): this.type = mutate {
    _validFeedback() = () => Some(<div>{v}</div>)
  }

  def validFeedbackStrF(f: () => String): this.type = mutate {
    _validFeedback() = () => Some(<div>{f()}</div>)
  }

  override def preRender(): Unit = {
    super.preRender()
    _validFeedback.setRendered()
  }

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().flatMap(js => _validFeedback.updateIfChanged({
      case (None, _) => scala.util.Failure(new Exception("Need to rerender"))
      case (Some(_), None) => scala.util.Success(js & JS.removeId(validFeedbackId))
      case (Some(_), Some(elem)) => scala.util.Success(js & JS.replace(validFeedbackId, elem.withId(validFeedbackId)))
    }, Success(Js.Void)))
}
