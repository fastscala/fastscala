package com.fastscala.components.form7.mixins

import com.fastscala.components.form7.Form7
import com.fastscala.components.utils.Mutable
import com.fastscala.core.FSContext
import com.fastscala.js.Js

import scala.util.{Failure, Try}
import scala.xml.{Elem, NodeSeq}


trait F7FieldWithHelp extends Mutable {
  var _help: () => Option[Elem] = () => None

  def help: Option[Elem] = _help()

  def help(v: Option[Elem]): this.type = mutate {
    _help = () => v
  }

  def help(v: Elem): this.type = mutate {
    _help = () => Some(v)
  }

  def help(v: String): this.type = mutate {
    _help = () => Some(<div>{v}</div>)
  }

  def helpStrF(f: this.type => String): this.type = mutate {
    _help = () => Some(<div>{f(this)}</div>)
  }

  def helpNsF(f: this.type => Elem): this.type = mutate {
    _help = () => Some(f(this))
  }
}