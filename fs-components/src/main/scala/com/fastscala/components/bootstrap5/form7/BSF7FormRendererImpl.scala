package com.fastscala.components.bootstrap5.form7

import com.fastscala.components.form7.mixins.StandardF7Field
import com.fastscala.components.form7.renderers.{F7FormRenderer, StandardOneInputElemF7FieldRenderer}
import com.fastscala.components.utils.Mutable
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.util.chaining.scalaUtilChainingOps
import scala.xml.{Elem, NodeSeq}

class BSF7FormRendererImpl extends F7FormRenderer with Mutable {

  import com.fastscala.components.bootstrap5.helpers.BSHelpers.*

  protected var onForm: Elem => Elem = identity[Elem]

  def onForm(f: Elem => Elem): this.type = mutate {
    onForm = onForm andThen f
  }

  override def render(contents: NodeSeq): Elem = div.apply(contents).mb_5.w_100.addClass("form").pipe(onForm)
}
