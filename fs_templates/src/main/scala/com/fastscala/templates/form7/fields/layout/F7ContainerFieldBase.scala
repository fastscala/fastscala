package com.fastscala.templates.form7.fields.layout


import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.templates.form7._
import com.fastscala.templates.form7.mixins._
import com.fastscala.xml.scala_xml.JS

import scala.xml.{Elem, NodeSeq}

abstract class F7ContainerFieldBase
  extends F7Field
    with F7FieldWithValidations
    with F7FieldWithEnabled
    with F7FieldWithDependencies
    with F7FieldWithDisabled
    with F7FieldWithReadOnly {

  def aroundClass: String

  def children: Seq[(String, F7Field)]

  var currentlyEnabled = enabled

  override def render()(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Elem = {
    currentlyEnabled = enabled
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
    if (enabled != currentlyEnabled) {
      JS.replace(aroundId, render())
    } else {
      children.map(_._2.reRender()).reduceOption[Js](_ & _).getOrElse(Js.void)
    }
  }

  override def fieldAndChildreenMatchingPredicate(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] =
    List(this).filter(_ => predicate.applyOrElse[F7Field, Boolean](this, _ => false)) :::
      children.toList.flatMap(_._2.fieldAndChildreenMatchingPredicate(predicate))

  override def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext, hints: Seq[RenderHint]): Js =
    super.onEvent(event) & children.map(_._2.onEvent(event)).reduceOption(_ & _).getOrElse(Js.void)
}
