package com.fastscala.templates.form7.fields

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7.mixins._
import com.fastscala.templates.form7._

import scala.xml.{Elem, NodeSeq}

class F7HtmlField(
                   gen: => NodeSeq
                 ) extends F7Field with F7FieldWithValidations
  with F7FieldWithDisabled
  with F7FieldWithReadOnly
  with F7FieldWithDependencies
  with F7FieldWithEnabled {

  override def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem =
    if (!enabled()) <div style="display:none;" id={aroundId}></div>
    else <div id={aroundId}>{gen}</div>

  override def fieldAndChildreenMatchingPredicate(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] = if (predicate.applyOrElse[F7Field, Boolean](this, _ => false)) List(this) else Nil
}

