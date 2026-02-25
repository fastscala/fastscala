package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.{F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.utils.IdGen

import scala.util.Success
import scala.xml.Elem


trait F7FieldWithValidFeedback extends F7FieldInputFieldMixin with Mutable {
  lazy val _defaultFeedbackId = "feedback_" + IdGen.id

  var _feedbackId: () => String = () => _defaultFeedbackId

  def feedbackId: String = _feedbackId()

  def feedbackId(v: String): this.type = mutate {
    _feedbackId = () => v
  }

  var _validFeedback: F7FieldMixinStatus[Option[Elem]] = F7FieldMixinStatus(None)

  def validFeedback: Option[Elem] = _validFeedback()

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
      case (Some(_), None) => scala.util.Success(js & JS.removeId(feedbackId))
      case (Some(_), Some(elem)) => scala.util.Success(js & JS.replace(feedbackId, elem.withId(feedbackId)))
    }, Success(Js.Void)))
}
