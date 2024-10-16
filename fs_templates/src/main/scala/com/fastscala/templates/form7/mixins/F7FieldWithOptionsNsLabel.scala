package com.fastscala.templates.form7.mixins

import com.fastscala.templates.utils.Mutable
import com.fastscala.xml.scala_xml.FSScalaXmlSupport

import scala.xml.NodeSeq


trait F7FieldWithOptionsNsLabel[T] extends Mutable {

  var _option2NodeSeq: T => NodeSeq = opt => FSScalaXmlSupport.fsXmlSupport.buildText(opt.toString)

  def option2NodeSeq(f: T => NodeSeq): this.type = mutate {
    _option2NodeSeq = f
  }

  def option2String(f: T => String): this.type = mutate {
    _option2NodeSeq = opt => FSScalaXmlSupport.fsXmlSupport.buildText(f(opt))
  }
}
