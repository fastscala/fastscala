package com.fastscala.components.form7.mixins

import com.fastscala.components.utils.Mutable

import scala.xml.Elem


trait F7FieldWithLabel extends Mutable {
  var _label: () => Option[Elem] = () => None

  def label: Option[Elem] = _label()

  def label(v: Option[Elem]): this.type = mutate {
    _label = () => v
  }
  
  def labelElemOptF(v: () => Option[Elem]): this.type = mutate {
    _label = () => v()
  }

  def label(v: Elem): this.type = mutate {
    _label = () => Some(v)
  }
  
  def labelElemF(v: () => Elem): this.type = mutate {
    _label = () => Some(v())
  }

  def label(v: String): this.type = mutate {
    _label = () => Some(<label>{v}</label>)
  }

  def labelStrF(f: () => String): this.type = mutate {
    _label = () => Some(<label>{f()}</label>)
  }

  override def toString: String = label.map(_.toString).getOrElse(super.toString)
}