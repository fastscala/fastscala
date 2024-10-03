package com.fastscala.templates.form7.mixins

import com.fastscala.templates.form7.fields.text.F7FieldInputFieldMixin
import com.fastscala.xml.scala_xml.FSScalaXmlSupport

import scala.xml.{Elem, NodeSeq}


trait F7FieldWithLabel extends F7FieldInputFieldMixin {
  var _label: () => Option[NodeSeq] = () => None

  def label() = _label()

  def label(v: Option[NodeSeq]): this.type = mutate {
    _label = () => v
  }

  def label(v: NodeSeq): this.type = mutate {
    _label = () => Some(v)
  }

  def label(v: String): this.type = mutate {
    _label = () => Some(FSScalaXmlSupport.fsXmlSupport.buildText(v))
  }

  def labelNodeSeqF(f: () => Option[NodeSeq]): this.type = mutate {
    _label = f
  }

  def labelStrF(f: () => String): this.type = mutate {
    _label = () => Some(<span>{f()}</span>)
  }

  def withLabel(label: String): this.type = mutate {
    _label = () => Some(<span>{label}</span>)
  }

  def withLabel(label: Elem): this.type = mutate {
    _label = () => Some(label)
  }
}