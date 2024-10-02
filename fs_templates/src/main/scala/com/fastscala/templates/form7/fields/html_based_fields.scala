package com.fastscala.templates.form7.fields

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7.Form7

import scala.xml.{Elem, NodeSeq}


class F7RawHtmlField(
                      gen: => NodeSeq
                    ) extends StandardF7Field
  with F7FieldWithDisabled
  with F7FieldWithReadOnly
  with F7FieldWithDependencies
  with F7FieldWithEnabled {

  override def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem =
    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else <div id={aroundId}>{gen}</div>

  override def fieldsMatching(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] = if (predicate.applyOrElse[F7Field, Boolean](this, _ => false)) List(this) else Nil
}

class F7SurroundWithHtmlField[T <: F7Field](
                                             wrap: Elem => Elem
                                           )(
                                             field: T
                                           )
  extends StandardF7Field
    with F7FieldWithReadOnly
    with F7FieldWithDependencies
    with F7FieldWithDisabled
    with F7FieldWithEnabled {
  override def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem =
    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else <div id={aroundId}>{wrap(field.render())}</div>

  override def fieldsMatching(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] =
    List(this).filter(_ => predicate.applyOrElse[F7Field, Boolean](this, _ => false)) :::
      List(field).flatMap(_.fieldsMatching(predicate))

  override def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = field.onEvent(event)
}

