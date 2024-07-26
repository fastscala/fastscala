package com.fastscala.templates.form6.fields

import com.fastscala.core.FSXmlUtils.EnrichSeqNodeSeq
import com.fastscala.core.{FSContext, FSXmlEnv, FSXmlSupport}
import com.fastscala.js.Js
import com.fastscala.templates.form6.Form6
import com.fastscala.xml.scala_xml.FSScalaXmlSupport.RichElem

import scala.xml.NodeSeq

class F6VerticalField[E <: FSXmlEnv]()(children: F6Field[E]*)(implicit fsXmlSupport: FSXmlSupport[E])
  extends StandardF6Field[E]
    with F6FieldWithEnabled[E]
    with F6FieldWithDependencies[E]
    with F6FieldWithDisabled[E]
    with F6FieldWithReadOnly[E] {

  var currentlyEnabled = enabled()

  override def render()(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): E#Elem = {
    currentlyEnabled = enabled()
    if (!currentlyEnabled) <div style="display:none;" id={aroundId}></div>.asFSXml()
    else implicitly[FSXmlSupport[E]].buildElem("div", "id" -> aroundId)(children.map(_.render()).mkNS())
  }

  override def reRender()(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): Js = {
    if (enabled() != currentlyEnabled) {
      Js.replace(aroundId, render())
    } else {
      children.map(_.reRender()).reduceOption[Js](_ & _).getOrElse(Js.void)
    }
  }

  override def fieldsMatching(predicate: PartialFunction[F6Field[E], Boolean]): List[F6Field[E]] =
    List(this).filter(_ => predicate.applyOrElse[F6Field[E], Boolean](this, _ => false)) :::
      children.toList.flatMap(_.fieldsMatching(predicate))

  override def onEvent(event: FormEvent)(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & children.map(_.onEvent(event)).reduceOption(_ & _).getOrElse(Js.void)
}

object F6VerticalField {
  def apply[E <: FSXmlEnv : FSXmlSupport]()(children: F6Field[E]*) = new F6VerticalField[E]()(children: _*)
}

class F6HorizontalRowField[E <: FSXmlEnv]()(children: (String, F6Field[E])*)(implicit fsXmlSupport: FSXmlSupport[E])
  extends StandardF6Field[E]
    with F6FieldWithEnabled[E]
    with F6FieldWithDependencies[E]
    with F6FieldWithDisabled[E]
    with F6FieldWithReadOnly[E] {

  var currentlyEnabled = enabled()

  override def render()(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): E#Elem = {
    currentlyEnabled = enabled()
    if (!currentlyEnabled) <div style="display:none;" id={aroundId}></div>.asFSXml()
    else {
      withFieldRenderHints { implicit hints =>
        val contents = children.map({
          case (clas, field) => <div class={clas}>{field.render()}</div>
        }).reduceOption[NodeSeq](_ ++ _).getOrElse(NodeSeq.Empty)
        <div class="row" id={aroundId}>{contents}</div>.asFSXml()
      }
    }
  }

  override def reRender()(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): Js = {
    if (enabled() != currentlyEnabled) {
      Js.replace(aroundId, render())
    } else {
      children.map(_._2.reRender()).reduceOption[Js](_ & _).getOrElse(Js.void)
    }
  }

  override def fieldsMatching(predicate: PartialFunction[F6Field[E], Boolean]): List[F6Field[E]] =
    List(this).filter(_ => predicate.applyOrElse[F6Field[E], Boolean](this, _ => false)) :::
      children.toList.flatMap(_._2.fieldsMatching(predicate))

  override def onEvent(event: FormEvent)(implicit form: Form6[E], fsc: FSContext, hints: Seq[RenderHint]): Js =
    super.onEvent(event) & children.map(_._2.onEvent(event)).reduceOption(_ & _).getOrElse(Js.void)
}

object F6HorizontalRowField {
  def apply[E <: FSXmlEnv : FSXmlSupport]()(children: (String, F6Field[E])*) = new F6HorizontalRowField[E]()(children: _*)
}
