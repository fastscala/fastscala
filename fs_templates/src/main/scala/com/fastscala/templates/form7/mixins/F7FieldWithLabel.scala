package com.fastscala.templates.form7.mixins

import com.fastscala.templates.utils.Mutable

import scala.xml.Elem


trait F7FieldWithLabel extends Mutable {
  var _label: () => Option[Elem] = () => None

  def label: Option[Elem] = _label()

  def label(v: Option[Elem]): this.type = mutate {
    _label = () => v
  }

  def label(v: Elem): this.type = mutate {
    _label = () => Some(v)
  }

  def label(v: String): this.type = mutate {
    _label = () => Some(<label>{v}</label>)
  }

  def labelStrF(f: () => String): this.type = mutate {
    _label = () => Some(<label>{f()}</label>)
  }
}