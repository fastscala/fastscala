package com.fastscala.components.form7.mixins

import com.fastscala.components.bootstrap5.helpers.BSHelpers.RichElemBootstrapClasses
import com.fastscala.components.form7.{F7Field, F7FieldMixinStatus, Form7}
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.utils.IdGen

import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, NodeSeq}


trait F7FieldWithHelp extends F7Field with Mutable {
  lazy val _defaultHelpId = "help_" + IdGen.id

  var _helpId: () => String = () => _defaultHelpId

  def helpId: String = _helpId()

  def helpId(v: String): this.type = mutate {
    _helpId = () => v
  }

  private val _help: F7FieldMixinStatus[Option[Elem]] = F7FieldMixinStatus(None)

  def help: Option[Elem] = _help().map(_.withId(helpId))

  def help(v: Option[Elem]): this.type = mutate {
    _help() = () => v
  }

  def help(v: Elem): this.type = mutate {
    _help() = () => Some(v)
  }

  def help(v: String): this.type = mutate {
    _help() = () => Some(<div>{v}</div>)
  }

  def helpStrF(f: this.type => String): this.type = mutate {
    _help() = () => Some(<div>{f(this)}</div>)
  }

  def helpNsF(f: this.type => Elem): this.type = mutate {
    _help() = () => Some(f(this))
  }

  override def preRender(): Unit = {
    super.preRender()
    _help.setRendered()
  }

  override def updateFieldWithoutReRendering()(implicit form: Form7, fsc: FSContext): scala.util.Try[Js] =
    super.updateFieldWithoutReRendering().flatMap(js => _help.updateIfChanged({
      case (None, _) => scala.util.Failure(new Exception("Need to rerender"))
      case (Some(_), None) => scala.util.Success(js & JS.removeId(helpId))
      case (Some(_), Some(elem)) => scala.util.Success(js & JS.replace(helpId, elem.withId(helpId)))
    }, Success(Js.Void)))
}