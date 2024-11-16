package com.fastscala.templates.form7.fields

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.js.JS
import com.fastscala.templates.form7.mixins.*
import com.fastscala.templates.form7.{F7Event, F7Field, Form7, RenderHint}
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem

import scala.xml.Elem


class F7HtmlSurroundField[T <: F7Field](
                                         surround: Elem => Elem
                                       )(
                                         field: T
                                       )
  extends F7Field with F7FieldWithValidations
    with F7FieldWithReadOnly
    with F7FieldWithDependencies
    with F7FieldWithDisabled
    with F7FieldWithEnabled {

  override def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem =
    if (!enabled) <div style="display:none;" id={aroundId}></div>
    else <div id={aroundId}>{surround(field.render())}</div>

  override def fieldAndChildreenMatchingPredicate(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] =
    List(this).filter(_ => predicate.applyOrElse[F7Field, Boolean](this, _ => false)) :::
      List(field).flatMap(_.fieldAndChildreenMatchingPredicate(predicate))

  override def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = field.onEvent(event)
}
