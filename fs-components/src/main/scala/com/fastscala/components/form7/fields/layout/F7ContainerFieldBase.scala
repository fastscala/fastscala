package com.fastscala.components.form7.fields.layout

import com.fastscala.components.form7.*
import com.fastscala.components.form7.mixins.*
import com.fastscala.components.form7.mixins.mainelem.*
import com.fastscala.components.form7.mixins.mainelem.F7FieldWithDisabled
import com.fastscala.core.FSContext
import com.fastscala.js.Js
import com.fastscala.scala_xml.ScalaXmlElemUtils.RichElem
import com.fastscala.scala_xml.js.{JS, printBeforeExec}

import scala.xml.{Elem, NodeSeq}

abstract class F7ContainerFieldBase extends F7Field with F7FieldWithValidationRules with F7FieldWithEnabled with F7FieldWithDependencies with F7FieldWithDisabled with F7FieldWithReadOnly {

  def aroundClass: String

  def children: Seq[(String, F7Field)]

  var currentlyEnabled = enabled

  override protected def renderImpl()(implicit form: Form7, fsc: FSContext): Elem = {
    currentlyEnabled = enabled
    if (!currentlyEnabled) <div style="display:none;" id={aroundId}></div>
    else {
      val contents = children.map({
        case (clas, field) if field.enabled => <div class={clas} id={field.aroundId + "_container_wrapper"}>{field.render()}</div>
        case (clas, field) if !field.enabled => <div class={clas} id={field.aroundId + "_container_wrapper"} style="display:none;">{field.render()}</div>
      }).reduceOption[NodeSeq](_ ++ _).getOrElse(NodeSeq.Empty)
      <div class={aroundClass} id={aroundId}>{contents}</div>
    }
  }

  override def onEvent(event: F7Event)(implicit form: Form7, fsc: FSContext): Js = (event match {
    case EnabledStatusChanged(field) if children.exists(_._2 == field) && field.enabled => JS.show(field.aroundId + "_container_wrapper")
    case EnabledStatusChanged(field) if children.exists(_._2 == field) && !field.enabled => JS.hide(field.aroundId + "_container_wrapper")
    case _ => JS.void
  }) & super.onEvent(event) & children.map(_._2.onEvent(event)).reduceOption(_ & _).getOrElse(JS.void)

  override def reRender()(implicit form: Form7, fsc: FSContext): Js = {
    if (enabled != currentlyEnabled) {
      JS.replace(aroundId, render())
    } else {
      children.map(_._2.reRender()).reduceOption[Js](_ & _).getOrElse(JS.void)
    }
  }

  override def fieldAndChildreenMatchingPredicate(predicate: PartialFunction[F7Field, Boolean]): List[F7Field] =
    List(this).filter(_ => predicate.applyOrElse[F7Field, Boolean](this, _ => false)) :::
      children.toList.flatMap(_._2.fieldAndChildreenMatchingPredicate(predicate))
}
