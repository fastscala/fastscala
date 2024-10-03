package com.fastscala.templates.form7.fields.layout

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7._
import com.fastscala.templates.form7.mixins._
import com.fastscala.xml.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromElems
import com.fastscala.xml.scala_xml.{FSScalaXmlSupport, JS}

import scala.xml.Elem

class F7VerticalField()(children: F7Field*)
  extends StandardF7Field
    with F7FieldWithEnabled
    with F7FieldWithDependencies
    with F7FieldWithDisabled
    with F7FieldWithReadOnly {

  var currentlyEnabled = enabled()

  override def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    currentlyEnabled = enabled()
    if (!currentlyEnabled) <div style="display:none;" id={aroundId}></div>
    else FSScalaXmlSupport.fsXmlSupport.buildElem("div", "id" -> aroundId)(children.map(_.render()).mkNS)
  }

  override def reRender()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = {
    if (enabled() != currentlyEnabled) {
      JS.replace(aroundId, render())
    } else {
      children.map(_.reRender()).reduceOption[Js](_ & _).getOrElse(Js.void)
    }
  }

  override def fieldAndChildreenMatchingPredicate(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] =
    List(this).filter(_ => predicate.applyOrElse[F7Field, Boolean](this, _ => false)) :::
      children.toList.flatMap(_.fieldAndChildreenMatchingPredicate(predicate))

  override def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & children.map(_.onEvent(event)).reduceOption(_ & _).getOrElse(Js.void)
}

object F7VerticalField {
  def apply()(children: F7Field*) = new F7VerticalField()(children: _*)
}
