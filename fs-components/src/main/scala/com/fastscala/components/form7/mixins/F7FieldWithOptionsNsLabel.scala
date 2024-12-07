package com.fastscala.components.form7.mixins

import com.fastscala.components.utils.Mutable

import scala.xml.NodeSeq


trait F7FieldWithOptionsNsLabel[T] extends Mutable {

  var _option2NodeSeq: T => NodeSeq = opt => scala.xml.Text(opt.toString)

  def option2NodeSeq(f: T => NodeSeq): this.type = mutate {
    _option2NodeSeq = f
  }

  def option2String(f: T => String): this.type = mutate {
    _option2NodeSeq = opt => scala.xml.Text(f(opt))
  }
}
