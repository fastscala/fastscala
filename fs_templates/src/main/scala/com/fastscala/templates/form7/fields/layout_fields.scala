package com.fastscala.templates.form7.fields

import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7.Form7
import com.fastscala.xml.scala_xml.ScalaXmlNodeSeqUtils.MkNSFromElems
import com.fastscala.xml.scala_xml.{FSScalaXmlSupport, JS}

import scala.xml.{Elem, NodeSeq}

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

  override def fieldsMatching(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] =
    List(this).filter(_ => predicate.applyOrElse[F7Field, Boolean](this, _ => false)) :::
      children.toList.flatMap(_.fieldsMatching(predicate))

  override def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = super.onEvent(event) & children.map(_.onEvent(event)).reduceOption(_ & _).getOrElse(Js.void)
}

object F7VerticalField {
  def apply()(children: F7Field*) = new F7VerticalField()(children: _*)
}

abstract class F7ContainerFieldBase
  extends StandardF7Field
    with F7FieldWithEnabled
    with F7FieldWithDependencies
    with F7FieldWithDisabled
    with F7FieldWithReadOnly {

  def aroundClass: String

  def children: Seq[(String, F7Field)]

  var currentlyEnabled = enabled()

  override def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    currentlyEnabled = enabled()
    if (!currentlyEnabled) <div style="display:none;" id={aroundId}></div>
    else {
      withFieldRenderHints { implicit hints =>
        val contents = children.map({
          case (clas, field) => <div class={clas}>{field.render()}</div>
        }).reduceOption[NodeSeq](_ ++ _).getOrElse(NodeSeq.Empty)
        <div class={aroundClass} id={aroundId}>{contents}</div>
      }
    }
  }

  override def reRender()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js = {
    if (enabled() != currentlyEnabled) {
      JS.replace(aroundId, render())
    } else {
      children.map(_._2.reRender()).reduceOption[Js](_ & _).getOrElse(Js.void)
    }
  }

  override def fieldsMatching(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] =
    List(this).filter(_ => predicate.applyOrElse[F7Field, Boolean](this, _ => false)) :::
      children.toList.flatMap(_._2.fieldsMatching(predicate))

  override def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js =
    super.onEvent(event) & children.map(_._2.onEvent(event)).reduceOption(_ & _).getOrElse(Js.void)
}

class F7ContainerField(val aroundClass: String)(val children: (String, F7Field)*) extends F7ContainerFieldBase

object F7ContainerField {
  def apply(aroundClass: String)(children: (String, F7Field)*) = new F7ContainerField(aroundClass)(children: _*)
}
