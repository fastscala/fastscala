package com.fastscala.templates.form7.mixins

import com.fastscala.templates.utils.Mutable

import scala.xml.Elem


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

  def helpStrF(f: () => String): this.type = mutate {
    _help = () => Some(<div>{f()}</div>)
  }
}