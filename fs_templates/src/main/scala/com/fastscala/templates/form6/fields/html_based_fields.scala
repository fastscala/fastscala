package com.fastscala.templates.form6.fields

import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.templates.form6.Form6
import com.fastscala.xml.scala_xml.FSScalaXmlSupport.RichElem


class F6RawHtmlField[E <: FSXmlEnv](
                                     gen: => E#NodeSeq
                                   )(implicit fsXmlSupport: FSXmlSupport[E]) extends StandardF6Field[E]
  with F6FieldWithDisabled[E]
  with F6FieldWithReadOnly[E]
  with F6FieldWithDependencies[E]
  with F6FieldWithEnabled[E] {

  override def render()(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): E#Elem =
    if (!enabled()) <div style="display:none;" id={aroundId}></div>.asFSXml()
    else <div id={aroundId}>{gen}</div>.asFSXml()

  override def fieldsMatching(predicate: PartialFunction[F6Field[E], Boolean]): List[F6Field[E]] = if (predicate.applyOrElse[F6Field[E], Boolean](this, _ => false)) List(this) else Nil
}

class F6SurroundWithHtmlField[E <: FSXmlEnv, T <: F6Field[E]](
                                                               wrap: E#Elem => E#Elem
                                                             )(
                                                               field: T
                                                             )(implicit fsXmlSupport: FSXmlSupport[E])
  extends StandardF6Field[E]
    with F6FieldWithReadOnly[E]
    with F6FieldWithDependencies[E]
    with F6FieldWithDisabled[E]
    with F6FieldWithEnabled[E] {
  override def render()(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): E#Elem =
    if (!enabled()) <div style="display:none;" id={aroundId}></div>.asFSXml()
    else <div id={aroundId}>{wrap(field.render())}</div>.asFSXml()

  override def fieldsMatching(predicate: PartialFunction[F6Field[E], Boolean]): List[F6Field[E]] =
    List(this).filter(_ => predicate.applyOrElse[F6Field[E], Boolean](this, _ => false)) :::
      List(field).flatMap(_.fieldsMatching(predicate))

  override def onEvent(event: FormEvent)(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): Js = field.onEvent(event)
}

